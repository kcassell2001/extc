/*
 * Copyright (c) 2008, the JUNG Project and the Regents of the University
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
/*
 * Created on 2008-06-26
 */
package edu.uci.ics.jung.algorithms.layout;

import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;

import java.awt.Dimension;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LazyMap;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.decorators.AbstractEdgeShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape.IndexedRendering;

/**
 * DAGLayout is a layout algorithm which is suitable for tree-like directed
 * acyclic graphs. It will not accept cyclic graphs. The layout will result in
 * directed edges pointing downwards. Any vertices with no predecessors are
 * considered to be level 0, and will be fixed in the top of the layout. Any
 * vertex has a level one greater than the maximum level of all its successors.
 *
 * It is an implementation of the algorithm in the book <it>Graph Drawing</it>,
 * by Giuseppe di Battista, Peter Eades, Roberto Tamassia and Ioannigs G.
 * Tollis.
 *
 * It was created to work with relatively small graphs, so the algorithms here
 * are not optimized (they are not even of the best complexity class possible
 * for the problem). Also, recursion is used in some algorithms, so the depth of
 * the DAG is also somewhat limited (~8000 levels ? :-) ).
 *
 * As this class uses virtual vertices to route the edges around the other
 * vertices, the EdgeShapeTransformer used must be aware of virtual vertices.
 * With this intent a BentLine class is nested inside this class.
 *
 * FIXME: It does not accept parallel edges
 *
 * FIXME: The layout does not take into account locked vertices.
 *
 * FIXME: It is still not very good at minimizing edge crossings. The
 * orderVertice() method has a lot of room to improvements.
 *
 * FIXME: The final layout does not take into account multi-graphs (unconnected
 * components), so when laid-out they will be somewhat entangled. This another
 * thing that can be improved in grapicalLayout().
 *
 * FIXME: It would be good to include an EdgeShapeTransformer that draws curved
 * lines, instead of straight edges.
 *
 * FIXME: It does not accept modification of the graph "on the fly", that is,
 * after the layout.
 *
 * TODO: After a step of edge direction inversion, the same algorithm can be
 * used to layout cyclic graphs.
 *
 * @author rstarr
 * @creation 2008-06-26
 *
 */
public class DAGLayoutBattista<V extends Comparable<V>, E extends Comparable<E>>
        extends AbstractLayout<V, E> {
   
    /* Some basic layout restrictions */
    private static final int MINIMUM_SEPARATION_BETWEEN_LAYERS = 20;
    private static final int MINIMUM_SEPARATION_BETWEEN_VERTICES = 20;
   
    /* The final locations */
    protected Map<Virtualizable<V>, Point2D> locations = LazyMap.decorate(
            new HashMap<Virtualizable<V>, Point2D>(),
            new Transformer<Virtualizable<V>, Point2D>() {
                public Point2D transform(Virtualizable<V> arg0) {
                    return new Point2D.Double();
                }
            });
   
    /* A copy of the graph is needed so we can keep track of virtual edges */
    DirectedGraph<Virtualizable<V>, Virtualizable<E>> virtualGraph;
   
    /* Some other graph info */
    protected HashSet<Virtualizable<V>> roots = new HashSet<Virtualizable<V>>();
   
    /* From the root layer we can navigate to the others */
    protected Layer<V> rootLayer;
   
    /*
     * Associates the layer number to each edge. Used to fastly find the layer
     * number of a given edge.
     */
    protected HashMap<Virtualizable<V>, Integer> vertexLevel = new HashMap<Virtualizable<V>, Integer>();
   
    /*
     * Edges spanning more than two layer are broken in multiple edges
     * connecting virtual vertices. This list keeps track of the edges that
     * suffered this process, so the edge drawer can recover it to draw them
     * correctly.
     */
    protected HashMap<Virtualizable<E>, List<Virtualizable<V>>> virtualEdges = new HashMap<Virtualizable<E>, List<Virtualizable<V>>>();
   
    /**
     * @see edu.uci.ics.jung.algorithms.layout.AbstractLayout
     */
    public DAGLayoutBattista(Graph<V, E> graph) {
        super(graph, new Dimension(1, 1));
        initialize();
    }
   
    /**
     * @see edu.uci.ics.jung.algorithms.layout.AbstractLayout
     */
    public DAGLayoutBattista(Graph<V, E> graph, Transformer<V, Point2D> initializer) {
        super(graph, initializer);
        initialize();
    }
   
    /**
     * @see edu.uci.ics.jung.algorithms.layout.AbstractLayout
     */
    public DAGLayoutBattista(Graph<V, E> graph, Dimension size) {
        super(graph, size);
        initialize();
    }
   
    /**
     * @see edu.uci.ics.jung.algorithms.layout.AbstractLayout
     */
    public DAGLayoutBattista(Graph<V, E> graph, Transformer<V, Point2D> initializer,
            Dimension size) {
        super(graph, initializer, size);
        initialize();
    }
   
    /**
     * Carry on the tasks to prepare the layout. These include:
     * <ol>
     * <li>Creating a copy of the graph so we can include virtual vertices
     * <li>Find the roots of the graph
     * <li>Assign a layer to each vertex
     * <li>Create the virtual vertices for the edges spanning more than two
     * layers
     * <li>Order the vertices to reduce edge crossings
     * <li>Assign a real 2D position for each vertex
     * </ol>
     *
     */
    public void initialize() {
        createVirtualGraph();
        findRoots();
        assignLayers();
        createVirtualVertices();
        orderVertices();
        graphicalLayout();
    }
   
    /**
     * Creates a graph that is a copy of the previous one, but that allows for
     * the inclusion of virtual vertices.
     */
    protected void createVirtualGraph() {
        this.virtualGraph = new DirectedSparseGraph<Virtualizable<V>, Virtualizable<E>>();
        Graph<V, E> graph = getGraph();
        Pair<V> pair;
       
        for (V v : graph.getVertices()) {
            this.virtualGraph.addVertex(new Virtualizable<V>(v));
        }
        for (E e : graph.getEdges()) {
            pair = graph.getEndpoints(e);
            this.virtualGraph.addEdge(new Virtualizable<E>(e),
                    new Virtualizable<V>(pair.getFirst()),
                    new Virtualizable<V>(pair.getSecond()));
        }
    }
   
    /**
     * Calculate each root of the graph. A root is any vertex of the graph that
     * has no incoming vertex.
     */
    protected void findRoots() {
        for (Virtualizable<V> v : this.virtualGraph.getVertices()) {
            Collection<Virtualizable<V>> predecessors = this.virtualGraph
                    .getPredecessors(v);
            if (predecessors.size() == 0) {
                this.roots.add(v);
            }
        }
        if (this.roots.size() == 0) {
            throw new Error("This graph is not acyclic.");
        }
    }
   
    /**
     * For each of the roots found in the graph, distribute the following
     * vertices in the layers, using a maximu depth approach.
     *
     */
    protected void assignLayers() {
       
        this.rootLayer = new Layer<V>(
                (DirectedGraph<Virtualizable<V>, ?>) this.virtualGraph, null,
                null);
        for (Virtualizable<V> root : this.roots) {
            assignLayers(root, rootLayer);
        }
    }
   
    /**
     * A recursive method for allocating the layer for each vertex. Recursively
     * scans the graph, in a depth first manner, assigning each vertex to a
     * deeper layer.
     *
     * @param v
     *            The current vertex
     * @param layer
     *            The level of the current vertex
     * @param this.vertexLevel A list of all the vertex that have already been
     *        visited
     */
    protected void assignLayers(Virtualizable<V> v, Layer<V> layer) {
       
        int previousLayerOfNode;
        int thisLevel = layer.getLevel();
        layer.add(v);
        this.vertexLevel.put(v, thisLevel);
       
        for (Virtualizable<V> child : this.virtualGraph.getSuccessors(v)) {
            if (this.vertexLevel.containsKey(child)) {
                previousLayerOfNode = this.vertexLevel.get(child);
                if (thisLevel >= previousLayerOfNode) {
                    layer.getLayerOnLevel(previousLayerOfNode).remove(child);
                    this.vertexLevel.remove(child);
                    // When calling assignLayers again, all the descendants of
                    // this node will be reassigned
                    // FIXME: This algorithm is not efficient at all!
                } else {
                    /*
                     * We stop the iteration if the child is already in a level
                     * bigger than this one
                     */
                    return;
                }
            }
            assignLayers(child, layer.getNextLayer(true));
        }
    }
   
    /**
     * In the layout of a layered digraph it is interesting that no vertices
     * span more than one layer. So, to avoid this situation, we add virtual
     * vertices. These will in fact be the restrictions to the path of the arcs
     * so they can go around the nodes in intermediate layers.
     *
     * This method scans every edge and, for each one that spans more than two
     * layers, adds the virtual nodes.
     */
    protected void createVirtualVertices() {
       
        // Create a copy of the old edges.
        Collection<Virtualizable<E>> oldEdges = new LinkedList<Virtualizable<E>>(
                this.virtualGraph.getEdges());
       
        // This will hold a list of the virtual vertices of an edge
        LinkedList<Virtualizable<V>> list;
        Pair<Virtualizable<V>> pair;
        Virtualizable<V> first, second, virtual;
        int layerFirst, layerSecond;
       
        for (Virtualizable<E> e : oldEdges) {
            pair = this.virtualGraph.getEndpoints(e);
            first = pair.getFirst();
            second = pair.getSecond();
            layerFirst = this.vertexLevel.get(first);
            layerSecond = this.vertexLevel.get(second);
           
            if (layerSecond - layerFirst > 1) {
                list = new LinkedList<Virtualizable<V>>();
                this.virtualEdges.put(e, list);
               
                // We know that second is always under first
                while (layerSecond - layerFirst > 1) {
                    virtual = new Virtualizable<V>(null, true);
                    list.add(virtual);
                   
                    this.rootLayer.getLayerOnLevel(layerFirst + 1).add(virtual);
                    this.vertexLevel.put(virtual, layerFirst + 1);
                   
                    this.virtualGraph.removeEdge(e);
                    this.virtualGraph.addEdge(new Virtualizable<E>(null, true),
                            first, virtual);
                    this.virtualGraph.addEdge(new Virtualizable<E>(null, true),
                            virtual, second);
                   
                    // Now we change the elements so we keep breaking the edges
                    // longer than one layer
                    layerFirst += 1;
                    first = virtual;
                    e = this.virtualGraph.findEdge(first, second);
                }
            }
           
        }
    }
   
    /**
     * Sweeps each layer (but the first) and order the vertices
     */
    protected void orderVertices() {
        Layer<V> currentLayer = this.rootLayer.getNextLayer(false);
        while (currentLayer != null) {
            orderVertices(currentLayer);
            currentLayer = currentLayer.getNextLayer(false);
        }
    }
   
    /**
     * Order the vertices in this layer using a layer-by-layer sweep and an
     * adjacent-exchange algorithm. Repeats the ordering until the total
     * crossing number for the layer does not reduce any further.
     */
    protected void orderVertices(Layer<V> layer) {
       
        // No need to order only one vertex
        if (layer.size() < 2) {
            return;
        }
       
        boolean crossingChanged = true;
        Virtualizable<V> u, v;
        List<Virtualizable<V>> oldOrdering;
        Iterator<Virtualizable<V>> it;
       
        while (crossingChanged) {
            crossingChanged = false;
            oldOrdering = new LinkedList<Virtualizable<V>>(layer.getVertices());
           
            it = oldOrdering.iterator();
            u = it.next();
            while (it.hasNext()) {
                v = it.next();
                if (layer.crossings(u, v) > layer.crossings(v, u)) {
                    layer.exchange(u, v);
                    crossingChanged = true;
                }
                u = v; // Step to the next node
            }
        }
    }
   
    /**
     * Assign a 2D position for each node. This position is based on the
     * requested dimensions of the graph respecting a minimum separation between
     * nodes and layers and trying to distribute the layers and the vertices
     * evenly across the drawing area.
     *
     * FIXME: This layout will not give a good layout for multi-graphs, because
     * it does not consider each multi-graph as an separate entity.
     *
     */
    protected void graphicalLayout() {
        int layerNumber = 0;
        int vertexNumber = 0;
       
        double separationBetweenVertices;
        double separationBetweenLayers = this.getSize().height / (this.rootLayer
                                                 .getRemainingLevels() + 1);
        double start = -separationBetweenLayers / 2;
       
        if (separationBetweenLayers < MINIMUM_SEPARATION_BETWEEN_LAYERS) {
            separationBetweenLayers = MINIMUM_SEPARATION_BETWEEN_LAYERS;
        }
       
        Layer<V> currentLayer = this.rootLayer;
        while (currentLayer != null) {
            layerNumber++;

            separationBetweenVertices = this.getSize().width / (currentLayer
                                                .size() + 1);
            if (separationBetweenVertices < MINIMUM_SEPARATION_BETWEEN_VERTICES) {
                separationBetweenVertices = MINIMUM_SEPARATION_BETWEEN_VERTICES;
            }
           
            vertexNumber = 0;
            for (Virtualizable<V> v : currentLayer.getVertices()) {
                vertexNumber++;
                setLocation(v, vertexNumber * separationBetweenVertices,
                        layerNumber * separationBetweenLayers
                                + start);
            }
            currentLayer = currentLayer.getNextLayer(false);
        }
       
    }
   
    /**
     * Returns the Coordinates object that stores the vertex' x and y location.
     *
     * @param v
     *            A Vertex that is a part of the Graph being visualized.
     * @return A Coordinates object with x and y locations.
     */
    protected Point2D getCoordinates(V v) {
        if (v != null) {
            return getCoordinates(new Virtualizable<V>(v));
        } else {
            return new Point2D.Double();
        }
    }
   
    /**
     * Returns the Coordinates object that stores the vertex' x and y location.
     *
     * @param v
     *            A Vertex that is a part of the Graph being visualized.
     * @return A Coordinates object with x and y locations.
     */
    protected Point2D getCoordinates(Virtualizable<V> v) {
        return locations.get(v);
    }
   
    /**
     * Returns the x coordinate of the vertex from the Coordinates object. in
     * most cases you will be better off calling getLocation(Vertex v);
     *
     * @see edu.uci.ics.jung.algorithms.layout.Layout#getX(edu.uci.ics.jung.graph.Vertex)
     */
    public double getX(V v) {
        assert getCoordinates(v) != null : "Cannot getX for an unmapped vertex " + v;
        return getCoordinates(v).getX();
    }
   
    /**
     * Returns the y coordinate of the vertex from the Coordinates object. In
     * most cases you will be better off calling getLocation(Vertex v)
     *
     * @see edu.uci.ics.jung.algorithms.layout.Layout#getX(edu.uci.ics.jung.graph.Vertex)
     */
    public double getY(V v) {
        assert getCoordinates(v) != null : "Cannot getY for an unmapped vertex " + v;
        return getCoordinates(v).getY();
    }
   
    /**
     * Returns the location of a given vertex.
     *
     * @param v
     *            a Vertex of interest
     * @return the location point of the supplied vertex
     */
    public Point2D getLocation(V v) {
        return getCoordinates(v);
    }
   
    /**
     * Forcibly moves a vertex to the (x,y) location by setting its x and y
     * locations to the inputted location. Does not add the vertex to the
     * "dontmove" list, and (in the default implementation) does not make any
     * adjustments to the rest of the graph.
     */
    public void setLocation(V picked, double x, double y) {
        Point2D coord = getCoordinates(picked);
        coord.setLocation(x, y);
    }
   
    public void setLocation(V picked, Point2D p) {
        Point2D coord = getCoordinates(picked);
        coord.setLocation(p);
    }
   
    public void setLocation(Virtualizable<V> picked, double x, double y) {
        Point2D coord = getCoordinates(picked);
        coord.setLocation(x, y);
    }
   
    public Point2D transform(V v) {
        return getCoordinates(v);
    }
   
    /**
     * @see edu.uci.ics.jung.algorithms.layout.Layout#reset()
     */
    public void reset() {
    }
   
    /**
     * Draws an edge that is a series of straight lines connecting the control
     * virtual points that make an edge. This is necessary if the edges should
     * route through the vertices in the intermediary layers.
     */
    public static class BentLine<V extends Comparable<V>, E extends Comparable<E>>
            extends AbstractEdgeShapeTransformer<V, E> implements
            IndexedRendering<V, E> {
       
    	/* Singleton instance of the BentLine shape */
        private static GeneralPath instance = new GeneralPath();
       
        /* Indexing of parallel edges */
        protected EdgeIndexFunction<V, E> parallelEdgeIndexFunction;
       
        /* The layout will be used to get the virtual vertex information */
        protected DAGLayoutBattista<V, E> layout;
       
        /**
         * Will produce edges for the given DAGLayout.
         *
         * @param layout
         */
        public BentLine(DAGLayoutBattista<V, E> layout) {
            this.layout = layout;
        }
       
        public void setEdgeIndexFunction(
                EdgeIndexFunction<V, E> parallelEdgeIndexFunction) {
            this.parallelEdgeIndexFunction = parallelEdgeIndexFunction;
        }
       
        /**
         * @return the parallelEdgeIndexFunction
         */
        public EdgeIndexFunction<V, E> getEdgeIndexFunction() {
            return parallelEdgeIndexFunction;
        }
       
        /**
         * Creates the edge, routing it through all the virtual vertices
         * associated to that edge.
         */
        public Shape transform(Context<Graph<V, E>, E> context) {
            Virtualizable<E> e = new Virtualizable<E>(context.element);
           
            if (this.layout.virtualEdges.containsKey(e)) {
               
                Point2D coord, firstPoint, lastPoint;
               
                Pair<V> pair = context.graph.getEndpoints(context.element);
                Virtualizable<V> first = new Virtualizable<V>(pair.getFirst());
                Virtualizable<V> second = new Virtualizable<V>(pair.getSecond());
                AffineTransform translation = new AffineTransform();
                AffineTransform transform = new AffineTransform();
               
                instance.reset();
                // Adds the path coordinates (in the standard coordinate frame)
                firstPoint = this.layout.getCoordinates(first);
                instance.moveTo(0.0f, 0.0f);
                translation.setToTranslation(-firstPoint.getX(), -firstPoint
                        .getY());
               
                // Adds the coordinates of the virtual vertices
                for (Virtualizable<V> v : this.layout.virtualEdges.get(e)) {
                    coord = translation.transform(
                            this.layout.getCoordinates(v), null);
                    instance.lineTo((float) coord.getX(), (float) coord.getY());
                }
                lastPoint = translation.transform(this.layout
                        .getCoordinates(second), null);
                instance.lineTo((float) lastPoint.getX(), (float) lastPoint
                        .getY());
                // finish
               
                /*
                 * Now we need to scale the line according to the position of
                 * the virtual vertices.
                 */
                double x2 = lastPoint.getX();
                double y2 = lastPoint.getY();
               
                double theta = -atan2(y2, x2);
                double scale = 1 / sqrt(x2 * x2
                                        + y2
                                        * y2);
               
                transform.setToScale(scale, 1.0d);
                transform.rotate(theta);
                instance.transform(transform);
                for (Virtualizable<V> v : this.layout.virtualEdges.get(e)) {
                    coord = translation.transform(
                            this.layout.getCoordinates(v), null);
                }
               
            } else {
                // This edge does not go through any virtual vertex
                instance.reset();
                instance.moveTo(0.0f, 0.0f);
                instance.lineTo(1.0f, 1.0f);
            }
           
            return instance;
        }
    }
}


/**
 * This class can wrap a type and be used when virtual objects of that type must
 * be created. Its usefulness is mainly connected to the ability to create
 * virtual vertices and edges while laying out a graph. It is used both for
 * vertices and edges.
 *
 * @author rstarr
 *
 * @param <V>
 *            The class that will be wrapped.
 */
class Virtualizable<V extends Comparable<V>> implements
        Comparable<Virtualizable<V>> {
   
    // This is used to generate an individual id for each virtual node
    static long virtualIdGenerator = 0;
   
    V v;
    boolean virtual = false;
   
    // When this node is virtual, this method will be used to compare for
    // equality.
    long virtualId;
   
    public Virtualizable(V v) {
        this.v = v;
    }
   
    public Virtualizable(V v, boolean virtual) {
        this.virtual = virtual;
        this.v = v;
        // Generate an unique id for this vertex
        this.virtualId = Virtualizable.virtualIdGenerator++;
    }
   
    public String toString() {
        if (this.virtual) {
            return "<_:" + this.virtualId
                   + ">";
        } else {
            return "<" + v.toString()
                   + ">";
        }
    }
   
    public boolean equals(Object o) {
        if (o instanceof Virtualizable<?>) {
            Virtualizable<?> other = (Virtualizable<?>) o;
            if (this.virtual) {
                if (other.virtual) {
                    return this.virtualId == other.virtualId;
                } else {
                    return false; // Virtual vertices are ALWAYS different
                    // from non-virtual ones
                }
            } else {
                return this.v.equals(other.v);
            }
        }
        return false;
    }
   
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Virtualizable<V> o) {
        if (this.virtual) {
            if (o.virtual) {
                return (new Long(this.virtualId)).compareTo(o.virtualId);
            } else {
                // A virtual node is always smaller than a non-virtual one.
                return -1;
            }
        } else {
            return this.v.compareTo(o.v);
        }
    }
   
    @Override
    public int hashCode() {
        if (this.virtual) {
            // Unique hash for each virtual node
            return this.toString().hashCode();
        } else {
            return this.v.hashCode();
        }
    }
}

/**
 * Represents a layer in the layering of a DAG. It provides for the exchange of
 * position between two vertices in the layer, to calculate the degree of
 * crossing between two vertices and to find the layer directly before and
 * after.
 *
 * @author RSTARR
 *
 */
class Layer<V extends Comparable<V>> {
    private Layer<V> previousLayer;
    private Layer<V> nextLayer;
    private LinkedList<Virtualizable<V>> vertices = new LinkedList<Virtualizable<V>>();
    private DirectedGraph<Virtualizable<V>, ?> graph;
   
    public Layer(DirectedGraph<Virtualizable<V>, ?> graph,
            Layer<V> previousLayer, Layer<V> nextLayer) {
        this.graph = graph;
        this.previousLayer = previousLayer;
        this.nextLayer = nextLayer;
    }
   
    /**
     * @param previousLayerOfNode
     * @return
     */
    protected Layer<V> getLayerOnLevel(int layer) {
        if (getLevel() == layer) {
            return this;
        } else if (getLevel() > layer && this.previousLayer != null) {
            return this.previousLayer.getLayerOnLevel(layer);
        } else if (getLevel() < layer && this.nextLayer != null) {
            return this.nextLayer.getLayerOnLevel(layer);
        } else {
            return null;
        }
    }
   
    /**
     * @return
     */
    public int size() {
        return this.vertices.size();
    }
   
    /**
     * True if there is no layer before this one
     *
     * @return
     */
    public boolean isFirstLayer() {
        return this.previousLayer == null;
    }
   
    /**
     * True if there is no layer after this one.
     *
     * @return
     */
    public boolean isLastLayer() {
        return this.nextLayer == null;
    }
   
    /**
     * Returns the next layer. If the create argument is true and this layer is
     * the last layer, a new layer will be created and returned.
     *
     * @param create
     *            If true, a new layer will be created if this is the last
     *            layer.
     * @return The next layer, or null if this is the last layer and create is
     *         <code>false</code>.
     */
    public Layer<V> getNextLayer(boolean create) {
        if (isLastLayer() && create) {
            this.nextLayer = new Layer<V>(this.graph, this, null);
        }
        return this.nextLayer;
    }
   
    /**
     * Returns the previous layer. If the create argument is true and this layer
     * is the first layer, a new layer will be created and returned.
     *
     * @param create
     *            If true, a new layer will be created if this is the first
     *            layer.
     * @return The previous layer, or null if this is the first layer and create
     *         is <code>false</code>.
     */
    public Layer<V> getPreviousLayer(boolean create) {
        if (isFirstLayer() && create) {
            this.previousLayer = new Layer<V>(this.graph, null, this);
        }
        return this.previousLayer;
    }
   
    /**
     * Return the level in which this layer is. The first layer has level 0.
     *
     * @return The level in which this layer is.
     */
    public int getLevel() {
        if (isFirstLayer()) {
            return 0;
        } else {
            return 1 + this.previousLayer.getLevel();
        }
    }
   
    /**
     * Return the number of levels separating this layer from the last layer.
     */
    public int getRemainingLevels() {
        if (isLastLayer()) {
            return 0;
        } else {
            return 1 + this.nextLayer.getRemainingLevels();
        }
    }
   
    /**
     * Get an ordered collection of the vertices in this layer
     *
     * @return
     */
    public List<Virtualizable<V>> getVertices() {
        return this.vertices;
    }
   
    /**
     * Add a vertex as the last vertex of this layer
     *
     * @param v
     */
    public void add(Virtualizable<V> v) {
        this.vertices.add(v);
    }
   
    /**
     * Add a vertex as the last vertex of this layer
     *
     * @param v
     */
    public void remove(Virtualizable<V> v) {
        this.vertices.remove(v);
    }
   
    /**
     * Exchange the position of two vertices u and v.
     *
     * @param u
     * @param v
     */
    public void exchange(Virtualizable<V> u, Virtualizable<V> v) {
        int posU = this.vertices.indexOf(u);
        int posV = this.vertices.indexOf(v);
       
        this.vertices.set(posV, u);
        this.vertices.set(posU, v);
    }
   
    /**
     * Return the number of vertices that arrive in u that are crossed by
     * vertices arriving in v. If this is the first layer, returns always 0.
     *
     * @param u
     * @param v
     * @return The number of crossings
     */
    public int crossings(final Virtualizable<V> u, final Virtualizable<V> v) {
        if (isFirstLayer()) {
            return 0;
        } else {
            int posU = this.vertices.indexOf(u);
            int posV = this.vertices.indexOf(v);
           
            if (posU == posV) {
                return 0;
            } else {
                int crossing = 0;
               
                Collection<Virtualizable<V>> inVerticesU = this.graph
                        .getPredecessors(u);
                Collection<Virtualizable<V>> inVerticesV = this.graph
                        .getPredecessors(v);
               
                for (Virtualizable<V> w : inVerticesU) {
                    for (Virtualizable<V> z : inVerticesV) {
                        if (this.previousLayer.getPos(z) < this.previousLayer
                                .getPos(w)) {
                            crossing++;
                        }
                    }
                }
                return crossing;
            }
        }
    }
   
    private int getPos(Virtualizable<V> v) {
        return this.vertices.indexOf(v);
    }
}
