    package org.apache.usergrid.drivers.blueprints;

    import com.fasterxml.jackson.databind.JsonNode;
    import com.tinkerpop.blueprints.Direction;
    import com.tinkerpop.blueprints.Edge;
    import com.tinkerpop.blueprints.Vertex;
    import com.tinkerpop.blueprints.VertexQuery;
    import org.apache.usergrid.java.client.Client;
    import org.apache.usergrid.java.client.model.UsergridEntity;
    import org.apache.usergrid.java.client.response.ApiResponse;

    import java.io.IOException;
    import java.util.*;

    /**
    * Created by ApigeeCorporation on 6/29/15.
    */
    public class UsergridVertex extends UsergridEntity implements Vertex{
    private static String CONNECTIONING = "connecting" ;
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
    * @param direction
    * @param labels
    * @return
    */
    public Iterable<Edge> getEdges(Direction direction, String... labels) {

    /**
    1) Check if the vertex exists.
    2) Get the UUIDs of edges that are connected to the
    particular vertex in a particular direction and with a particular label
    3) Return an iterable of edges
    */

    ValidationUtils.validateNotNull(direction, IllegalArgumentException.class, "Direction for getEdges cannot be null");
    ValidationUtils.validateNotNull(labels,IllegalArgumentException.class, "Label for edge in getEdges cannot be null");
    ValidationUtils.validateStringNotEmpty(labels.toString(), RuntimeException.class, "Label for edge in getEdges cannot be empty");

    String srcType = this.getType();
    String srcId = this.getUuid().toString();
    List<Edge> edgesSet1 = new ArrayList<Edge>();
        List<Edge> edgesSet2 = new ArrayList<Edge>();

        ApiResponse response = UsergridGraph.client.queryEdgesForVertex(srcType, srcId);

    ValidationUtils.serverError(response, IOException.class,"Usergrid server error");
    ValidationUtils.validateAccess(response,RuntimeException.class,"User forbidden from using the Usergrid resource");
    ValidationUtils.validateCredentials(response, RuntimeException.class, "User credentials for Usergrid are invalid");
    ValidationUtils.validateRequest(response, RuntimeException.class, "Invalid request passed to Usergrid");
    ValidationUtils.OrgAppNotFound(response, RuntimeException.class, "Organization or application does not exist in Usergrid");

    UsergridEntity trgUUID = response.getFirstEntity();

    switch (direction) {
        case OUT:
            if (!checkHasEdges(trgUUID, CONNECTIONS)) {
                return null;
            }
            IterarteOverEdges(trgUUID, srcType, srcId, edgesSet1, CONNECTIONS);
            return edgesSet1;

        case IN:
            if (!checkHasEdges(trgUUID, CONNECTIONING)) {
                return null;
            }
            IterarteOverEdges(trgUUID, srcType, srcId, edgesSet2, CONNECTIONING);
            return edgesSet2;
        case BOTH:
            if (!checkHasEdges(trgUUID, CONNECTIONS)) {
                return null;
            }
            if (!checkHasEdges(trgUUID, CONNECTIONING)) {
                return null;
            }
            IterarteOverEdges(trgUUID, srcType, srcId, edgesSet1, CONNECTIONS);
            IterarteOverEdges(trgUUID, srcType, srcId, edgesSet2, CONNECTIONING);
            edgesSet1.addAll(edgesSet2);
            return edgesSet1;
    }


    return null;
    }

    private boolean checkHasEdges(UsergridEntity trgUUID, String CONNECTIONS) {
    if(trgUUID.getProperties().get(METADATA).findValue(CONNECTIONS) == null)
    return false;
    else
    return true;
    }

    private void IterarteOverEdges(UsergridEntity trgUUID, String srcType, String srcId, List<Edge> edges, String conn) {
    Iterator<String> connections = trgUUID.getProperties().get(METADATA).findValue(conn).fieldNames();
    Direction direction = null;
    while (connections.hasNext()){

    String name = connections.next();
    ApiResponse resp = null;
    if(conn == CONNECTIONS) {
     resp = UsergridGraph.client.queryConnection(srcType, srcId, name);
     direction = Direction.OUT;
    }
    else {
     resp = UsergridGraph.client.queryConnection(srcType, srcId, CONNECTIONING, name);
     direction = Direction.IN;
    }
    List<UsergridEntity> entities = resp.getEntities();
    edges = getAllEdgesForVertex(entities, name, edges,direction);
    }
    }

    private List<Edge> getAllEdgesForVertex(List<UsergridEntity> entities, String name,List<Edge> edges, Direction dir) {
    for (int i = 0; i < entities.size(); i++) {
        UsergridEntity e = entities.get(i);
    String v = e.getType() + SLASH + e.getStringProperty("name");
    Edge e1 = null;
    if (dir == Direction.OUT)
      e1 = new UsergridEdge(this.getId().toString(),v,name);
    else if (dir == Direction.IN)
      e1 = new UsergridEdge(v,this.getId().toString(),name);
    edges.add(e1);
    }
    return edges;
    }



    /**
    * This gets all the adjacent vertices connected to the vertex by an edge specified by a particular direction and label
    *
    * @param direction
    * @param labels
    * @return
    */
    public Iterable<Vertex> getVertices(Direction direction, String... labels) {
    /**
    1) Check if the vertex exists
    2) Get the UUIDs of edges that are connected to the
    particular vertex in a particular direction and with a particular label
    3)Get the vertices at the other end of the edge
    4) Return an iterable of vertices
    */
    return null;
    }

    /**
    * Generate a query object that can be
    * used to fine tune which connections/entities are retrieved that are incident/adjacent to this entity.
    *
    * @return
    */
    public VertexQuery query() {
    return null;
    }


    /**
    * Adds an edge to the vertex, with the target vertex specified
    *
    * @param label
    * @param inVertex
    * @return
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

    //TODO: What happens when an edge between two vertices already exists? Return the existing edge?
    ValidationUtils.serverError(response, IOException.class,"Usergrid server error");
    ValidationUtils.validateAccess(response, RuntimeException.class, "User forbidden from using the Usergrid resource");
    ValidationUtils.validateCredentials(response, RuntimeException.class, "User credentials for Usergrid are invalid");
    ValidationUtils.validateRequest(response, RuntimeException.class, "Invalid request passed to Usergrid");
    ValidationUtils.OrgAppNotFound(response, RuntimeException.class, "Organization or application does not exist in Usergrid");

      return e;
    }

    /**
    * Get a particular property of a vertex specified by a key
    *
    * @param key
    * @param <T>
    * @return
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
    * Get all the property keys for a particular vertex
    *
    * @return
    */
    public Set<String> getPropertyKeys() {

        //TODO: Check if vertex exists?

        Set<String> allKeys = super.getProperties().keySet();
    return allKeys;
    }



    public void onChanged(Client client) {

    }

    /**
    * This sets a particular value of a property using the specified key in the local object
    *
    * @param key
    * @param value
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
    } else {
    throw new IllegalArgumentException("Supplied id class of " + String.valueOf(value.getClass()) + " is not supported");
    }
    }

    public void setProperty(String key, Object value) {
        if (key.equals(STRING_TYPE)){
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
                ApiResponse responseDelete = UsergridGraph.client.deleteEntity(oldType,this.getUuid().toString());

                ApiResponse response = UsergridGraph.client.createEntity(v);

                ValidationUtils.serverError(response, IOException.class, "Usergrid server error");
                ValidationUtils.validateAccess(response, RuntimeException.class, "User forbidden from using the Usergrid resource");
                ValidationUtils.validateDuplicate(response, RuntimeException.class, "Entity with the name specified already exists in Usergrid");
                ValidationUtils.validateCredentials(response, RuntimeException.class, "User credentials for Usergrid are invalid");
                ValidationUtils.validateRequest(response, RuntimeException.class, "Invalid request passed to Usergrid");
                ValidationUtils.OrgAppNotFound(response, RuntimeException.class, "Organization or application does not exist in Usergrid");

                String uuid = response.getFirstEntity().getStringProperty(STRING_UUID);
                v.setUuid(UUID.fromString(uuid));
                if (allOUTEdges != null) {
                    for (Object outEdge : allOUTEdges) {
                        //TODO:Create outGoing Edges for the Vertex

                        String[] parts = ((UsergridEdge) outEdge).getId().toString().split(SLASH);
                        String sourceName = parts[1];
                        String connectionType = parts[2];
                        String target = parts[3]+SLASH+parts[4];
                        ApiResponse responseOutEdge = UsergridGraph.client.connectEntities(v.getType(),sourceName,connectionType,target);

                        ValidationUtils.serverError(responseOutEdge, IOException.class, "Usergrid server error");
                        ValidationUtils.validateAccess(responseOutEdge, RuntimeException.class, "User forbidden from using the Usergrid resource");
                        ValidationUtils.validateDuplicate(responseOutEdge, RuntimeException.class, "Entity with the name specified already exists in Usergrid");
                        ValidationUtils.validateCredentials(responseOutEdge, RuntimeException.class, "User credentials for Usergrid are invalid");
                        ValidationUtils.validateRequest(responseOutEdge, RuntimeException.class, "Invalid request passed to Usergrid");
                        ValidationUtils.OrgAppNotFound(responseOutEdge, RuntimeException.class, "Organization or application does not exist in Usergrid");

                    }
                }
                if (allINEdges != null) {
                    for (Object inEdge : allINEdges) {
                        //TODO: Create incomingEdges for the Vertex

                        String[] parts = ((UsergridEdge) inEdge).getId().toString().split(SLASH);
                        String sourceType = parts[0];
                        String sourceName = parts[1];
                        String connectionType = parts[2];
                        ApiResponse responseInEdge = UsergridGraph.client.connectEntities(sourceType,sourceName,connectionType,v.getId().toString());

                        ValidationUtils.serverError(responseInEdge, IOException.class, "Usergrid server error");
                        ValidationUtils.validateAccess(responseInEdge, RuntimeException.class, "User forbidden from using the Usergrid resource");
                        ValidationUtils.validateDuplicate(responseInEdge, RuntimeException.class, "Entity with the name specified already exists in Usergrid");
                        ValidationUtils.validateCredentials(responseInEdge, RuntimeException.class, "User credentials for Usergrid are invalid");
                        ValidationUtils.validateRequest(responseInEdge, RuntimeException.class, "Invalid request passed to Usergrid");
                        ValidationUtils.OrgAppNotFound(responseInEdge, RuntimeException.class, "Organization or application does not exist in Usergrid");

                    }
                }
            }
        }
        else {
            setLocalProperty(key, value);
            super.save();
        }
    }

    /**
    * Remove a particular property as specified by the key
    *
    * @param key
    * @param <T>
    * @return
    */
    public <T> T removeProperty(String key) {
        T oldValue = this.getProperty(key);

        super.setProperty(key, (String) null);
    return  oldValue;
    }

    /**
    * Removes or deletes the vertex or entity
    */
    public void remove() {

    super.delete();

    }

    /**
    * This gets the Id of the vertex
    *
    * @return
    */
    public Object getId() {
    String ObjectType = this.getType();
    UUID ObjectUUID = this.getUuid();
    String id;
    if (this.getProperty(STRING_NAME)!= null) {
        id = ObjectType + SLASH + this.getProperty(STRING_NAME);
    }
        else {
        id = ObjectType + SLASH + ObjectUUID;
    }
    return id;

    }

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


        @Override
        public int hashCode(){
            int hashCode = this.getId().hashCode();
            return hashCode;
        }

    }
