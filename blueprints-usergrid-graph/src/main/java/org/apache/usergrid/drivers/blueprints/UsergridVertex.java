package org.apache.usergrid.drivers.blueprints;

import com.fasterxml.jackson.databind.JsonNode;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import org.apache.usergrid.java.client.Client;
import org.apache.usergrid.java.client.model.UsergridEntity;
import org.apache.usergrid.java.client.response.ApiResponse;

import java.util.*;

/**
 * Created by ApigeeCorporation on 6/29/15.
 */
public class UsergridVertex extends UsergridEntity implements Vertex {
    private static String CONNECTING = "connecting";
    private static String defaultType;
    private static String METADATA = "metadata";
    private static String CONNECTIONS = "connections";
    public static final String SLASH = "/";
    public static String STRING_NAME = "name";
    public static String STRING_TYPE = "type";
    public static String STRING_UUID = "uuid";


    public UsergridVertex(String defaultType) {
        super.setType(defaultType);
    }

    public static void setDefaultType(String defaultType) {
        UsergridVertex.defaultType = defaultType;
    }

    /**
     * This gets edges that are connected to the vertex in a
     * particular direction specified, and having a specific label
     *
     * @param direction Direction of the edges to retrieve.
     * @param labels Names of the edges to retrieve.
     * @return Returns an Iterable of edges.
     */
    public Iterable<Edge> getEdges(Direction direction, String... labels) {

        /**
         1) Check if the vertex exists.
         2) Get the UUIDs of edges that are connected to the
         particular vertex in a particular direction and with a particular label
         3) Return an iterable of edges
         */

        ValidationUtils.validateNotNull(direction, IllegalArgumentException.class, "Direction for getEdges cannot be null");
        ValidationUtils.validateStringNotEmpty(labels.toString(), RuntimeException.class, "Label for edge in getEdges cannot be empty");

        String srcType = this.getType();
        String srcId = this.getUuid().toString();
        List<Edge> edgesSet1 = new ArrayList<Edge>();


        ApiResponse response = UsergridGraph.client.queryEdgesForVertex(srcType, srcId);
        UsergridGraph.ValidateResponseErrors(response);

        //Gets the vertex for which edges are to be found
        UsergridEntity trgEntity = response.getFirstEntity();

        switch (direction) {
            case OUT:
                if (!checkHasEdges(trgEntity, CONNECTIONS)) {
                    //Returns empty list if there are no edges
                    return new ArrayList<Edge>();
                }
                IterarteOverEdges(trgEntity, srcType, srcId, edgesSet1, CONNECTIONS, labels);
                return edgesSet1;

            case IN:
                if (!checkHasEdges(trgEntity, CONNECTING)) {
                    return new ArrayList<Edge>();
                }
                IterarteOverEdges(trgEntity, srcType, srcId, edgesSet1, CONNECTING, labels);
                return edgesSet1;
            case BOTH:
                if (!checkHasEdges(trgEntity, CONNECTIONS)) {
                    if (!checkHasEdges(trgEntity, CONNECTING)) {
                        return new ArrayList<Edge>();
                    }
                    IterarteOverEdges(trgEntity, srcType, srcId, edgesSet1, CONNECTING, labels);
                    return edgesSet1;
                } else if (!checkHasEdges(trgEntity, CONNECTING)) {
                    IterarteOverEdges(trgEntity, srcType, srcId, edgesSet1, CONNECTIONS, labels);
                    return edgesSet1;
                }
                IterarteOverEdges(trgEntity, srcType, srcId, edgesSet1, CONNECTING, labels);
                IterarteOverEdges(trgEntity, srcType, srcId, edgesSet1, CONNECTIONS, labels);
                return edgesSet1;
        }
        return new ArrayList<Edge>();
    }



    private boolean checkHasEdges(UsergridEntity trgUUID, String conn) {
        if (trgUUID.getProperties().get(METADATA).findValue(conn) == null)
            return false;
        else
            return true;
    }

    private void IterarteOverEdges(UsergridEntity trgUUID, String srcType, String srcId, List<Edge> edges, String conn, String... labels) {
        List<String> connections = new ArrayList<String>();
        //If labels are specified
        if(labels.length != 0){
            for (String label : labels){
                connections.add(label);
            }
        }else {
            //When labels are not specified
            Iterator<String> conn1 = trgUUID.getProperties().get(METADATA).findValue(conn).fieldNames();
            while(conn1.hasNext()){
                connections.add(conn1.next());
            }
        }
        Direction direction = null;
        for (int conLen = 0 ; conLen < connections.size();conLen++){
            ApiResponse resp = new ApiResponse();
                    if (conn == CONNECTIONS) {
                        resp = UsergridGraph.client.queryConnection(srcType, srcId, connections.get(conLen));
                        direction = Direction.OUT;
                    } else {
                        resp = UsergridGraph.client.queryConnection(srcType, srcId, CONNECTING, connections.get(conLen));
                        direction = Direction.IN;
                    }
                    List<UsergridEntity> entities = resp.getEntities();
                    getAllEdgesForVertex(entities, connections.get(conLen), edges, direction);
                }
    }

    private List<Edge> getAllEdgesForVertex(List<UsergridEntity> entities, String name, List<Edge> edges, Direction dir) {
        for (int i = 0; i < entities.size(); i++) {
            UsergridEntity e = entities.get(i);
            String vertex;
            if(e.getStringProperty(STRING_NAME) != null )
                vertex = e.getType() + SLASH + e.getStringProperty(STRING_NAME);
            else
                vertex = e.getType() + SLASH + e.getUuid().toString();
            Edge e1 = null;
            if (dir == Direction.OUT)
                e1 = new UsergridEdge(this.getId().toString(), vertex, name);
            else if (dir == Direction.IN)
                e1 = new UsergridEdge(vertex, this.getId().toString(), name);
            edges.add(e1);
        }
        return edges;
    }

    private void IterarteOverVertices(UsergridEntity trgEntity, String srcType, String srcId, List<Vertex> vertexSet, String conn, String... labels) {
        List<String> connections = new ArrayList<String>();
        //If labels are specified
        if(labels.length != 0){
            for (String label : labels){
                connections.add(label);
            }
        }else {
            //When labels are not specified, Example of conn1 is 'likes', 'hates' and other such verbs associated with the vertex
            Iterator<String> conn1 = trgEntity.getProperties().get(METADATA).findValue(conn).fieldNames();
            while(conn1.hasNext()){
                connections.add(conn1.next());
            }
        }
        for (int conLen = 0 ; conLen < connections.size();conLen++){
            ApiResponse resp = new ApiResponse();
            if (conn == CONNECTIONS) {
                resp = UsergridGraph.client.queryConnection(srcType, srcId, connections.get(conLen));
            } else {
                resp = UsergridGraph.client.queryConnection(srcType, srcId, CONNECTING, connections.get(conLen));
            }
            List<UsergridEntity> entities = resp.getEntities();
            getAllVerticesForVertex(entities, vertexSet);
        }
    }

    private List<Vertex> getAllVerticesForVertex(List<UsergridEntity> entities, List<Vertex> vertices){
        for (int i = 0; i < entities.size(); i++) {
            UsergridVertex v1 = UsergridGraph.CreateVertexFromEntity(entities.get(i));
            vertices.add(v1);
        }
        return vertices;
    }


    /**
     * This gets all the adjacent vertices connected to the vertex by an edge specified by a particular direction and label
     *
     * @param direction Direction of the vertices to retrieve.
     * @param labels names of the vertices to retrieve.
     * @return Returns and Iterable of vertices.
     */
    public Iterable<Vertex> getVertices(Direction direction, String... labels) {
        /**
         1) Check if the vertex exists
         2) Get the UUIDs of edges that are connected to the
         particular vertex in a particular direction and with a particular label
         3)Get the vertices at the other end of the edge
         4) Return an iterable of vertices
         */

        ValidationUtils.validateNotNull(direction, IllegalArgumentException.class, "Direction for getEdges cannot be null");
        ValidationUtils.validateStringNotEmpty(labels.toString(), RuntimeException.class, "Label for edge in getEdges cannot be empty");

        String srcType = this.getType().toString();
        String srcId = this.getUuid().toString();
        List<Vertex> vertexSet = new ArrayList<Vertex>();

        ApiResponse response = UsergridGraph.client.queryEdgesForVertex(srcType, srcId);
        UsergridGraph.ValidateResponseErrors(response);

        //Gets the vertex for which edges are to be found
        UsergridEntity trgEntity = response.getFirstEntity();

        switch (direction) {
            case OUT:
                if (!checkHasEdges(trgEntity, CONNECTIONS)) {
                    //Returns empty list if there are no adjacent vertices
                    return new ArrayList<Vertex>();
                }
                IterarteOverVertices(trgEntity, srcType, srcId, vertexSet, CONNECTIONS, labels);
                return vertexSet;

            case IN:
                if (!checkHasEdges(trgEntity, CONNECTING)) {
                    return new ArrayList<Vertex>();
                }
                IterarteOverVertices(trgEntity, srcType, srcId, vertexSet, CONNECTING, labels);
                return vertexSet;
            case BOTH:
                if (!checkHasEdges(trgEntity, CONNECTIONS)) {
                    if (!checkHasEdges(trgEntity, CONNECTING)) {
                        return new ArrayList<Vertex>();
                    }
                    IterarteOverVertices(trgEntity, srcType, srcId, vertexSet, CONNECTING, labels);
                    return vertexSet;
                } else if (!checkHasEdges(trgEntity, CONNECTING)) {
                    IterarteOverVertices(trgEntity, srcType, srcId, vertexSet, CONNECTIONS, labels);
                    return vertexSet;
                }
                IterarteOverVertices(trgEntity, srcType, srcId, vertexSet, CONNECTING, labels);
                IterarteOverVertices(trgEntity, srcType, srcId, vertexSet, CONNECTIONS, labels);
                return vertexSet;
        }
        return new ArrayList<Vertex>();
    }


    /**
     * Not supported for Usergrid
     *
     * Generate a query object that can be
     * used to filter which connections/entities are retrieved that are incident/adjacent to this entity.
     *
     * @return
     */
    public VertexQuery query() {
        throw new UnsupportedOperationException("Not supported for Usergrid");
    }


    /**
     * Adds an edge to the vertex, with the target vertex specified
     *
     * @param label Name of the edge to be added.
     * @param inVertex connecting edge.
     * @return Returns the new edge formed.
     */
    public Edge addEdge(String label, Vertex inVertex) {

    /**
    1) Check if the target vertex exists
    2) Use the following to add an edge - connectEntities( String connectingEntityType,String
    connectingEntityId, String connectionType, String connectedEntityId) in org.apache.usergrid.java.client
    3) Return the newly created edge
    */

    ValidationUtils.validateNotNull(label,IllegalArgumentException.class,"Label for edge cannot be null");
    ValidationUtils.validateNotNull(inVertex, IllegalArgumentException.class, "Target vertex cannot be null");
    ValidationUtils.validateStringNotEmpty(label, RuntimeException.class, "Label of edge cannot be emoty");

    UsergridEdge e = new UsergridEdge(this.getId().toString(), inVertex.getId().toString(), label);
    ApiResponse response = UsergridGraph.client.connectEntities(this, (UsergridVertex) inVertex, label);
    UsergridGraph.ValidateResponseErrors(response);

      return e;
    }

    /**
     * Get a particular property of a vertex specified by a key
     *
     * @param key The property to retrieve for a vertex.
     * @return Returns the value of the property.
     */
    public <T> T getProperty(String key) {

        /**
         1) Check if the vertex exists
         2) Use the getEntityProperty(String name, float/String/long/int/boolean/JsonNode value) in
         org.apache.usergrid.java.client.entities
         3) If any other type throw an error
         */

        //TODO: Check if vertex exists?

    ValidationUtils.validateNotNull(key, IllegalArgumentException.class, "Property key cannot be null");
    ValidationUtils.validateStringNotEmpty(key, RuntimeException.class, "Property key cannot be empty");

        T propertyValue = (T) super.getEntityProperty(key);

        //TODO: Check if property exists

        return propertyValue;
    }

    /**
     * This gets all the property keys for a particular vertex
     *
     * @return Returns a Set of properties for the vertex.
     */
    public Set<String> getPropertyKeys() {

        //TODO: Check if vertex exists?

        Set<String> allKeys = super.getProperties().keySet();
        return allKeys;
    }


    /**
     * This sets a particular value of a property using the specified key in the local object
     *
     * @param key Name of the property.
     * @param value Value of the property.
     */
    public void setLocalProperty(String key, Object value) {


    ValidationUtils.validateNotNull(key, IllegalArgumentException.class, "Key for the property cannot be null");

    ValidationUtils.validateStringNotEmpty(key, RuntimeException.class, "Key of the property cannot be empty");

    if (value instanceof String) {
    super.setProperty(key, (String) value);
    } else if (value instanceof JsonNode) {
    super.setProperty(key, (JsonNode) value);
    } else if (value instanceof Integer) {
    super.setProperty(key, (Integer) value);
    } else if (value instanceof Float) {
    super.setProperty(key, (Float) value);
    } else if (value instanceof Boolean) {
    super.setProperty(key, (Boolean) value);
    } else if (value instanceof Long) {
    super.setProperty(key, (Long) value);
    } else if (value.equals(null)){
    super.setProperty(key,(String) null);
    }
    else {
    throw new IllegalArgumentException("Supplied ID class of " + String.valueOf(value.getClass()) + " is not supported");
    }
    }

    public void setProperty(String key, Object value) {
        if (key.equals(STRING_TYPE)) {
            if (value.equals(this.getType())) {
            } else {
                String oldType = this.getType();
                String newType = value.toString();
                UsergridVertex v = new UsergridVertex(newType);
                Map allProperties = this.properties;
                Iterable allOUTEdges = this.getEdges(Direction.OUT);
                Iterable allINEdges = this.getEdges(Direction.IN);
                v.properties = allProperties;
                v.setLocalProperty(STRING_TYPE, newType);
                ApiResponse responseDelete = UsergridGraph.client.deleteEntity(oldType, this.getUuid().toString());

                ApiResponse response = UsergridGraph.client.createEntity(v);
                UsergridGraph.ValidateResponseErrors(response);
                ValidationUtils.validateDuplicate(response, RuntimeException.class, "Entity with the name specified already exists in Usergrid");

                String uuid = response.getFirstEntity().getStringProperty(STRING_UUID);
                v.setUuid(UUID.fromString(uuid));
                if (allOUTEdges != null) {
                    for (Object outEdge : allOUTEdges) {
                        //TODO:Create outGoing Edges for the Vertex
                        String[] parts = ((UsergridEdge) outEdge).getId().toString().split(SLASH);
                        String sourceName = parts[1];
                        String connectionType = parts[2];
                        String target = parts[3] + SLASH + parts[4];
                        ApiResponse responseOutEdge = UsergridGraph.client.connectEntities(v.getType(), sourceName, connectionType, target);
                        UsergridGraph.ValidateResponseErrors(responseOutEdge);
                        ValidationUtils.validateDuplicate(responseOutEdge, RuntimeException.class, "Entity with the name specified already exists in Usergrid");

                    }
                }
                if (allINEdges != null) {
                    for (Object inEdge : allINEdges) {
                        //TODO: Create incomingEdges for the Vertex

                        String[] parts = ((UsergridEdge) inEdge).getId().toString().split(SLASH);
                        String sourceType = parts[0];
                        String sourceName = parts[1];
                        String connectionType = parts[2];
                        ApiResponse responseInEdge = UsergridGraph.client.connectEntities(sourceType, sourceName, connectionType, v.getId().toString());
                        UsergridGraph.ValidateResponseErrors(responseInEdge);
                        ValidationUtils.validateDuplicate(responseInEdge, RuntimeException.class, "Entity with the name specified already exists in Usergrid");

                    }
                }
            }
        } else {
            setLocalProperty(key, value);
            super.save();
        }
    }

    /**
     * Remove a particular property as specified by the key
     *
     * @param key Name of the property to delete.
     * @return Returns the value of the property removed.
     */
    public <T> T removeProperty(String key) {
        T oldValue = this.getProperty(key);

        super.setProperty(key, (String) null);
        return oldValue;
    }

    /**
     * Removes or deletes the vertex or entity
     */
    public void remove() {

        super.delete();

    }

    /**
     * This gets the ID of the vertex
     *
     * @return Returns the ID of the vertex.
     */
    public Object getId() {
        String ObjectType = this.getType();
        UUID ObjectUUID = this.getUuid();
        String id;
        if (this.getProperty(STRING_NAME) != null) {
            id = ObjectType + SLASH + this.getProperty(STRING_NAME);
        } else {
            id = ObjectType + SLASH + ObjectUUID;
        }
        return id;

    }

    /**
     * This compares the vertex with another vertex by UUID and type
     * @param o The object tobe compared
     * @return Returns a Boolean whether the vertices are equal or not
     */
        @Override
        public boolean equals(Object o) {
            if (o instanceof UsergridVertex){
                UsergridVertex v = (UsergridVertex)o;
                boolean flag;
                if ((this.getUuid().equals(v.getUuid()))&&(this.getType().equals(v.getType()))){flag = true;}
                else {flag = false;}
                return flag;

            }
            else if (o instanceof Vertex) {
                Vertex v = (Vertex) o;
                boolean flag;
                if ((this.getProperty(STRING_UUID).equals(v.getProperty(STRING_UUID))) && (this.getProperty(STRING_TYPE).equals(v.getProperty(STRING_TYPE)))) {
                    flag = true;
                } else {
                    flag = false;
                }
                return flag;
            }

            throw new IllegalArgumentException("Couldn't compare class '" + o.getClass() + "'");
        }


    /**
     * This gives the hashCode of the ID of the vertex
     * @return Returns the hasCode of the ID of the vertex as an Integer
     */
        @Override
        public int hashCode(){
            int hashCode = this.getId().hashCode();
            return hashCode;
        }

}
