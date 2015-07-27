
package org.apache.usergrid.drivers.blueprints;

import com.fasterxml.jackson.databind.JsonNode;
import com.tinkerpop.blueprints.*;
import org.apache.commons.configuration.Configuration;
import org.apache.usergrid.java.client.Usergrid;
import org.apache.usergrid.java.client.model.UsergridEntity;
import org.apache.usergrid.java.client.response.ApiResponse;

import java.io.IOException;
import java.util.*;
import org.apache.log4j.Logger;

/**
 * Created by ApigeeCorporation on 6/29/15.
 */
public class UsergridGraph implements Graph {

    private static final int COUNT = 0;
    public static Usergrid client;
    private static String defaultType;
    private static int entityRetrivalCount;


    private static String METADATA = "metadata";
    private static String COLLECTIONS = "collections";
    private static String ROLES = "roles";
    private static final Logger log = Logger.getLogger(UsergridGraph.class);

    public static final String SLASH = "/";
    public static final String STRING_UUID = "uuid";
    public static final String STRING_NAME = "name";
    public static final String STRING_TYPE = "type";
    public static final String CONNECTOR = "/";
    private static Features features;

    static {

        features = new Features();
        /**
         * Does the graph allow for two edges with the same vertices and edge label to exist?
         */
        features.supportsDuplicateEdges = Boolean.FALSE;

        /**
         * Does the graph allow an edge to have the same out/tail and in/head vertex?
         */
        features.supportsSelfLoops = Boolean.FALSE;

        /**
         * Does the graph allow any serializable object to be used as a property value for a graph element?
         */
        features.supportsSerializableObjectProperty = Boolean.FALSE;

        /**
         * Does the graph allows boolean to be used as a property value for a graph element?
         */
        features.supportsBooleanProperty = Boolean.TRUE;

        /**
         * Does the graph allows double to be used as a property value for a graph element?
         */
        features.supportsDoubleProperty = Boolean.TRUE;

        /**
         * Does the graph allows float to be used as a property value for a graph element?
         */
        features.supportsFloatProperty = Boolean.TRUE;

        /**
         * Does the graph allows integer to be used as a property value for a graph element?
         */
        features.supportsIntegerProperty = Boolean.TRUE;

        /**
         * Does the graph allows a primitive array to be used as a property value for a graph element?
         */
        features.supportsPrimitiveArrayProperty = Boolean.TRUE;

        /**
         * Does the graph allows list (all objects with the list have the same data types) to be used as a property
         * value for a graph element?
         */
        features.supportsUniformListProperty = Boolean.TRUE;

        /**
         * Does the graph allows a mixed list (different data types within the same list) to be used as a
         * property value for a graph element?
         */
        features.supportsMixedListProperty = Boolean.FALSE;

        /**
         * Does the graph allows long to be used as a property value for a graph element?
         */
        features.supportsLongProperty = Boolean.TRUE;

        /**
         * Does the graph allows map to be used as a property value for a graph element?
         */
        features.supportsMapProperty = Boolean.TRUE;

        /**
         * Graph allows string to be used as a property value for a graph element.
         */
        features.supportsStringProperty = Boolean.TRUE;

        /**
         * Does the graph return elements not explicitly created with addVertex or addEdge?
         */
        features.hasImplicitElements = Boolean.TRUE;

        /**

         * Does the graph ignore user provided ids in graph.addVertex(Object id)?
         */
        features.ignoresSuppliedIds = Boolean.TRUE;

        /**
         * Does the graph persist the graph to disk after shutdown?
         */
        features.isPersistent = Boolean.TRUE;

        /**
         * Does the graph implement WrapperGraph?
         */
        features.isWrapper = Boolean.FALSE;

        /**
         * Does the graph implement IndexableGraph?
         */
        features.supportsIndices = Boolean.FALSE;

        /**
         * Does the graph support the indexing of vertices by their properties?
         */
        features.supportsVertexIndex = Boolean.FALSE;

        /**
         * Does the graph support the indexing of edges by their properties?
         */
        features.supportsEdgeIndex = Boolean.FALSE;

        /**
         * Does the graph implement KeyIndexableGraph?
         */
        features.supportsKeyIndices = Boolean.FALSE;

        /**
         * Does the graph support key indexing on vertices?
         */
        features.supportsVertexKeyIndex = Boolean.FALSE;

        /**
         * Does the graph support key indexing on edges?
         */
        features.supportsEdgeKeyIndex = Boolean.FALSE;

        /**
         * Does the graph support graph.getEdges()?
         */
        features.supportsEdgeIteration = Boolean.FALSE;

        /**
         * Does the graph support graph.getVertices()?
         */
        features.supportsVertexIteration = Boolean.FALSE;

        /**
         * Does the graph support retrieving edges by id, i.e. graph.getEdge(Object id)?
         */
        features.supportsEdgeRetrieval = Boolean.FALSE;

        /**
         * Does the graph support setting and retrieving properties on vertices?
         */
        features.supportsVertexProperties = Boolean.FALSE;

        /**
         * Does the graph support setting and retrieving properties on edges?
         */
        features.supportsEdgeProperties = Boolean.FALSE;

        /**
         * Does the graph implement TransactionalGraph?
         */
        features.supportsTransactions = Boolean.FALSE;

        /**
         * Does the graph implement ThreadedTransactionalGraph?
         */
        features.supportsThreadedTransactions = Boolean.FALSE;

        /**
         * Does the graph support transactions managed such that multiple threads operating on the same graph instance
         * can have isolated transactions?
         */
        features.supportsThreadIsolatedTransactions = Boolean.FALSE;
    }



    /**
     * @param config
     */
    public UsergridGraph(Configuration config) {

        //TODO: Change to appropriate location
        ValidationUtils.validateNotNull(config, RuntimeException.class, "Configuration for Usergrid cannot be null");
        this.defaultType = config.getString("usergrid.defaultType");
        this.entityRetrivalCount = config.getInt("usergrid.entityRetrivalCount");
        log.debug("UsergridGraph() : Setting the default type to : " + this.defaultType);

        //Configuration for Usergrid
        String orgName = config.getString("usergrid.organization");
        String appName = config.getString("usergrid.application");
        String apiUrl = config.getString("usergrid.apiUrl");
        String clientId = config.getString("usergrid.client_id");
        String clientSecret = config.getString("usergrid.client_secret");

        ValidationUtils.validateNotNull(orgName, RuntimeException.class, "Organization name in Usergrid cannot be null");
        ValidationUtils.validateNotNull(appName, RuntimeException.class, "Application name in Usergrid cannot be null");
        ValidationUtils.validateStringNotEmpty(orgName, RuntimeException.class, "Organization name cannot be empty in Usergrid");
        ValidationUtils.validateStringNotEmpty(appName, RuntimeException.class, "Application name cannot be empty in Usergrid");

        if (apiUrl == null)
            Usergrid.initialize(apiUrl, orgName, appName);
        else
            Usergrid.initialize(apiUrl, orgName, appName);
        log.debug("UsergridGraph() : Initializing the SingletonClient");

        //Get an instance of the client
        client = Usergrid.getInstance();
        ValidationUtils.validateNotNull(client, RuntimeException.class, "Client could not be instantiated.");

        //Authorize the Application with the credentials provided in the Configuration file
        client.authorizeAppClient(clientId, clientSecret);
        log.debug("UsergridGraph() : Authorizing the client application. Client is initialized with the application url : " + client.getApiUrl() + client.getOrganizationId());
    }


    /**
     * This returns all the features that the Blueprint supports for Usergrid.
     *
     * @return
     */
    public Features getFeatures() {
        log.debug("getFeatures() : The features set are : " + features);
        return features;
    }


    /**
     * This calls the client and creates a new entity "type:name". The collection for the entity is as specified
     * by 'type' and 'name' is the name of the entity. It returns the newly created vertex.
     *
     * @param id - The value of id.toString would be used for the name
     * @return the newly created vertex
     */
    public Vertex addVertex(Object id) {

    /*
    1) Check if client is initialized
    2) Check that id is of supported type, else throw IllegalArgumentException error
    3) Create the entity using an API call
    4) Return the newly created vertex
    */

        assertClientInitialized();

        String[] parts = new String[2];
        String VertexType = null;
        String VertexName = null;
        UsergridVertex v = null;
        if (id instanceof String) {
            log.debug("DEBUG addVertex(): id passed is an instance of string ");
            ValidationUtils.validateStringNotEmpty((String) id, RuntimeException.class, "id cannot be an empty string");
            if (id.toString().contains("/")) {
                parts = id.toString().split(SLASH);
                VertexType = parts[0];
                VertexName = parts[1];
                v = new UsergridVertex(VertexType);
                v.setLocalProperty(STRING_NAME, VertexName);
                v.setLocalProperty("_ugName", VertexName);
                v.setLocalProperty("_ugBlueprintsId", id);
            } else {
                v = new UsergridVertex(defaultType);
                VertexName = id.toString();
                v.setLocalProperty(STRING_NAME, VertexName);
                v.setLocalProperty("_ugName", VertexName);
                v.setLocalProperty("_ugBlueprintsId", id);
            }

        } else if (id instanceof Object) {
        log.debug("DEBUG addVertex(): id passed is an instance of object ");
        v = new UsergridVertex(defaultType);
        VertexName = id.toString();
        v.setLocalProperty(STRING_NAME, VertexName);
        v.setLocalProperty("_ugName", VertexName);
        v.setLocalProperty("_ugBlueprintsId", id);

        }
        else if (id == null){
        v = new UsergridVertex(defaultType);
        }
        else
        {
        log.error("ERROR addVertex(): id passed is in an invalid format.");
        throw new IllegalArgumentException("Supplied id class of " + String.valueOf(id.getClass()) + " is not supported by Usergrid");
        }


        ApiResponse response = client.createEntity(v);
        log.debug("DEBUG addVertex(): Api response returned for adding vertex is : " + response);

        ValidationUtils.serverError(response, IOException.class, "Usergrid server error");
        ValidationUtils.validateAccess(response, RuntimeException.class, "User forbidden from using the Usergrid resource");
        ValidationUtils.validateDuplicate(response, RuntimeException.class, "Entity with the name specified already exists in Usergrid");
        ValidationUtils.validateCredentials(response, RuntimeException.class, "User credentials for Usergrid are invalid");
        ValidationUtils.validateRequest(response, RuntimeException.class, "Invalid request passed to Usergrid");
        ValidationUtils.OrgAppNotFound(response, RuntimeException.class, "Organization or application does not exist in Usergrid");

        String uuid = response.getFirstEntity().getStringProperty(STRING_UUID);
        v.setUuid(UUID.fromString(uuid));

        log.debug("DEBUG addVertex(): Returning vertex with uuid : " + v.getUuid().toString());
        return v;

    }


    /**
     * This gets a particular Vertex (entity) using the ID of the vertex. The ID is in the form of "type:UUID",
     * where type is the collection type and UUId is the unique ID generated for each entity
     *
     * @param id
     * @return
     */
    public Vertex getVertex(Object id) {
    /*
    1) Check if client is initialized
    2) Check that id is of supported type, else throw IllegalArgumentException error
    3) Get and return the entity - Query queryEntitiesRequest(HttpMethod method,Map<String,
    Object> params, Object data, String... segments) in org.apache.usergrid.java.client
    4) Return null if no vertex is referenced by the identifier
    */

        assertClientInitialized();
        ValidationUtils.validateNotNull(id, RuntimeException.class, "id cannot be of type null");

        if (id instanceof String) {
            log.debug("DEBUG getVertex(): id is an instance of sting");
            ValidationUtils.validateStringNotEmpty((String) id, RuntimeException.class, "id cannot be an empty string");

            String[] parts = id.toString().split(SLASH);
            String type = parts[0];
            String StringUUID = parts[1];
            ApiResponse response = client.getEntity(type, StringUUID);
            log.debug("DEBUG getVertex(): Api response returned for query vertex is : " + response);

            ValidationUtils.serverError(response, IOException.class, "Usergrid server error");
            ValidationUtils.validateAccess(response, RuntimeException.class, "User forbidden from using the Usergrid resource");
            ValidationUtils.validateCredentials(response, RuntimeException.class, "User credentials for Usergrid are invalid");
            ValidationUtils.validateRequest(response, RuntimeException.class, "Invalid request passed to Usergrid");
            ValidationUtils.OrgAppNotFound(response, RuntimeException.class, "Organization or application does not exist in Usergrid");

            String uuid = response.getFirstEntity().getStringProperty(STRING_UUID);
            Map<String, JsonNode> vertexProperties = new HashMap<String, JsonNode>();
            vertexProperties = response.getFirstEntity().getProperties();
            UsergridVertex v = new UsergridVertex(type);
            v.setUuid(UUID.fromString(uuid));
            for (Map.Entry<String, JsonNode> entry : vertexProperties.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                v.setLocalProperty(key, value);

                log.debug("DEBUG getVertex(): Properties of the vertex : '" + v.getProperty(STRING_NAME) + "' got are : " + v.getProperties());
                log.debug("DEBUG getVertex(): Returning vertex with uuid : " + v.getUuid().toString());
                return v;
            }
        }
        throw new IllegalArgumentException("Supplied id class of " + String.valueOf(id.getClass()) + " is not supported by Usergrid");

    }

    /**
     * This deletes a particular vertex (entity) by taking the vertex as an identifier
     *
     * @param vertex
     */
    public void removeVertex(Vertex vertex) {

    /*
    1) Check if client is initialized
    2) Check if vertex exists
    3) Delete all edges connected to the vertex using disconnectEntities(String connectingEntityType,
    String connectingEntityId, String connectionType, String connectedEntityId) in org.apache.usergrid.java.client
    4) Delete the vertex //TODO: The method delete() is defined in org.apache.usergrid.java.client.entities but has not been implemented
    5) Return null if no vertex is referenced by the identifier
    */
        assertClientInitialized();
        ValidationUtils.validateNotNull(vertex, RuntimeException.class, "Vertex cannot be null");
        ValidationUtils.validateforVertex(vertex, RuntimeException.class, "Type of entity should be Vertex");
        String id = vertex.getId().toString();
        String[] parts = id.split(SLASH);
        String type = parts[0];
        String StringUUID = parts[1];
        ApiResponse response = client.deleteEntity(type, StringUUID);
        log.debug("DEBUG removeVertex(): Api response returned for remove vertex is : " + response);

        ValidationUtils.serverError(response, IOException.class, "Usergrid server error");
        ValidationUtils.validateAccess(response, RuntimeException.class, "User forbidden from using the Usergrid resource");
        ValidationUtils.validateCredentials(response, RuntimeException.class, "User credentials for Usergrid are invalid");
        ValidationUtils.validateRequest(response, RuntimeException.class, "Invalid request passed to Usergrid");
        ValidationUtils.OrgAppNotFound(response, RuntimeException.class, "Organization or application does not exist in Usergrid");
        log.debug("DEBUG removeVertex(): succesfully removed the vertex");

    }

    /**
     * {
     * throw new UnsupportedOperationException("Not supported for Usergrid");
     * }
     * Return an iterable to all the vertices in the graph that have a particular key/value property.
     *
     * @param key
     * @param value
     * @return
     */
    public Iterable<Vertex> getVertices(String key, Object value) {
        throw new UnsupportedOperationException("Not Supported in Usergris");
    }

    /**
     * Returns an iterable to all the vertices in the graph.
     *
     * @return : iterable to all the vertices in the graph
     */

    public Iterable<Vertex> getVertices() {
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("limit", entityRetrivalCount);
        List<Vertex> allVertices = new ArrayList<Vertex>();
        Iterator<Map.Entry<String, JsonNode>> collectionList;
        Map.Entry<String, JsonNode> collection;
        String collectionName;
        collectionList = getAllCollections();

        while (collectionList.hasNext()) {
            collection = collectionList.next();
            collectionName = collection.getKey();
            //TODO : exclude "roles" entity.Is there a standard list of collections we can ignore?
            if (collectionName != ROLES)
            {
                allVertices.addAll(GetVerticesForCollection(collectionName,paramsMap));
            }
        }
        return allVertices;
    }

    private List<Vertex> GetVerticesForCollection(String collectionName, Map<String, Object> paramsMap) {
        List<Vertex> allVertices =  new ArrayList<Vertex>();
        ApiResponse responseEntities = client.apiRequest("GET", paramsMap, null, client.getOrganizationId(), client.getApplicationId(), collectionName);
        AddIntoEntitiesArray(responseEntities.getEntities(), allVertices);
        while (responseEntities.getCursor() != null) {
            responseEntities = client.apiRequest("GET", paramsMap, null, client.getOrganizationId(), client.getApplicationId(), collectionName);
            AddIntoEntitiesArray(responseEntities.getEntities(), allVertices);
            paramsMap.put("cursor", responseEntities.getCursor());
        }
        return allVertices;
    }

    private Iterator<Map.Entry<String, JsonNode>> getAllCollections() {
        ApiResponse response = client.queryCollections();
        return response.getFirstEntity().getProperties().get(METADATA).get(COLLECTIONS).fields();
    }


    private List<Vertex> AddIntoEntitiesArray(List<UsergridEntity> entities, List<Vertex> allVertices) {

        Integer next = 0;
        if (entities.size() == 0) {
            return null;
        }
        while (entities.size() > next) {
            String type = entities.get(next).getType();
            String name = entities.get(next).getStringProperty("name");
            Vertex ugvertex = getVertex(type + "/" + name);
            allVertices.add(ugvertex);
            next++;
        }
        return allVertices;
    }


    /**
     * This function adds a connection (or an edge) between two vertices
     *
     * @param id
     * @param outVertex
     * @param inVertex
     * @param label
     * @return
     */
    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {

    /*
    1. Check client initialized.
    2. Check if the two vertices are valid.
    3. Retrieve the EntityIds of the two entities
    3. Call connectEntities( String connectingEntityType, String connectingEntityId, String connectionType, String connectedEntityId)
    4. Return the connection(or edge) // TODO : currently returns ApiResponse. Should return an edge.
    */

        assertClientInitialized();
        ValidationUtils.validateNotNull(outVertex, RuntimeException.class, "The vertex specified from where the Edge starts cannot be null");
        ValidationUtils.validateNotNull(inVertex, RuntimeException.class, "The vertex specified, where the Edge ends cannot be null");
        ValidationUtils.validateNotNull(label, RuntimeException.class, "Label for the edge cannot be null");
        ValidationUtils.validateStringNotEmpty(label, RuntimeException.class, "Label for the edge cannot be an empty string");

        UsergridEdge e = new UsergridEdge(outVertex.getId().toString(), inVertex.getId().toString(), label);
        UsergridVertex source = (UsergridVertex) outVertex;
        UsergridVertex target = (UsergridVertex) inVertex;
        ApiResponse response = client.connectEntities(source, target, label);
        log.debug("DEBUG addEdge(): Api response returned after add edge is : " + response);

        //updating the source and target vertex to reflect new properties.
        outVertex = getVertex(source.getId());
        log.debug("DEBUG getEdge(): source vertex with id : " + outVertex.getId() + "is updated");

        inVertex = getVertex(target.getId());
        log.debug("DEBUG getEdge(): target vertex with id : " + inVertex.getId() + "is updated");

        ValidationUtils.serverError(response, IOException.class, "Usergrid server error");
        ValidationUtils.validateAccess(response, RuntimeException.class, "User forbidden from using the Usergrid resource");
        ValidationUtils.validateDuplicate(response, RuntimeException.class, "Edge of the same type already exists between the two vertices in Usergrid");
        ValidationUtils.validateCredentials(response, RuntimeException.class, "User credentials for Usergrid are invalid");
        ValidationUtils.validateRequest(response, RuntimeException.class, "Invalid request passed to Usergrid");
        ValidationUtils.OrgAppNotFound(response, RuntimeException.class, "Organization or application does not exist in Usergrid");
        ValidationUtils.validateResourceExists(response, RuntimeException.class, "Resource does not exist in Usergrid");
        log.debug("DEBUG addEdge(): Returning Edge with id : " + e.getId());

        return e;

    }

    /**
     * This function returns a connection (or edge). Takes the Connection id as an input which is specified as
     * SourceVertexId-->connection-->TargetVertexId
     *
     * @param id
     * @return
     */
    public Edge getEdge(Object id) {

    /*
    1. Get the client. Check if client initialzed.
    2. Get the source vertex.
    3. Get the target vertex.
    4. Return the connection(or edge).
    */
        assertClientInitialized();
        ValidationUtils.validateNotNull(id, RuntimeException.class, "ID specified cannot be of type null");

        if (id instanceof String) {

            ValidationUtils.validateStringNotEmpty(id.toString(), RuntimeException.class, "ID cannot be an empty string");
            String[] properties = ((String) id).split(CONNECTOR);
            String label = properties[2];

            //Check if the edge is valid.
            ApiResponse response = client.apiRequest("GET", null, null, client.getOrganizationId(), client.getApplicationId(), id.toString());
            if (response.getError() != null) {
//                log.error("The get requested does not exists in the database.");
                throw new RuntimeException("The Edge requested does not exists in the database. Quitting... ");
            }

            Vertex srcVertex = getVertex(properties[0] + "/" + properties[1]);
            log.debug("DEBUG getEdge(): source vertex returned with id : " + srcVertex.getId());

            Vertex trgVertex = getVertex(properties[3] + "/" + properties[4]);
            log.debug("DEBUG getEdge(): target vertex returned with id : " + trgVertex.getId());

            client.queryConnection(properties);
            Edge connection = new UsergridEdge(srcVertex.getId().toString(), trgVertex.getId().toString(), label);
            log.debug("DEBUG addEdge(): Returning Edge with id : " + connection.getId());

            return connection;
        }
        throw new IllegalArgumentException("Supplied id class of " + String.valueOf(id.getClass()) + " is not supported by Usergrid");
    }


    /**
     * This function removes the connection between two entities in the graph. Takes the Connection
     * id as an input which is specified as SourceVertexId-->connection-->TargetVertexId
     *
     * @param edge
     */
    public void removeEdge(Edge edge) {

    /*
    1. Get the client. Check if its intitialzed.
    2. Get the connection(or edge) by the Id //TODO : how to retrieve an edge.
    3. Check if the edge is a valid edge.
    4. call disconnectEntities(String connectingEntityType, String connectingEntityId, String connectionType, String connectedEntityId)
    */

        assertClientInitialized();

        ValidationUtils.validateNotNull(edge, RuntimeException.class, "The edge specified cannot be null");

        String edgeId = edge.getId().toString();
        ValidationUtils.validateStringNotEmpty(edgeId, RuntimeException.class, "Unable to obtain the Edge ID of the edge specified");
        String[] properties = (edgeId).split(CONNECTOR);
        String label = properties[2];
        UsergridVertex srcVertex = (UsergridVertex) getVertex(properties[0] + "/" + properties[1]);
        UsergridVertex trgVertex = (UsergridVertex) getVertex(properties[3] + "/" + properties[4]);
        log.debug("DEBUG getvertEdge(): source vertex returned with id : " + srcVertex.getId());

        ApiResponse response = client.disconnectEntities(srcVertex, trgVertex, label);
        log.debug("DEBUG removeEdge(): Response returned from API call to disconnect Vertices is : " + response);

        ValidationUtils.serverError(response, IOException.class, "Usergrid server error");
        ValidationUtils.validateAccess(response, RuntimeException.class, "User forbidden from using the Usergrid resource");
        ValidationUtils.validateCredentials(response, RuntimeException.class, "User credentials for Usergrid are invalid");
        ValidationUtils.validateRequest(response, RuntimeException.class, "Invalid request passed to Usergrid");
        ValidationUtils.OrgAppNotFound(response, RuntimeException.class, "Organization or application does not exist in Usergrid");
        ValidationUtils.validateResourceExists(response, RuntimeException.class, "Resource does not exist in Usergrid");

        log.debug("DEBUG removeEdge(): exiting from remove edge.");
    }

    /**
     * Not Implemented for Usergrid
     *
     * @return
     */

    public Iterable<Edge> getEdges() {

        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("limit", entityRetrivalCount);
        List<Edge> allVertices = new ArrayList<Edge>();
        Iterator<Map.Entry<String, JsonNode>> collectionList;
        Map.Entry<String, JsonNode> collection;
        String collectionName;
        collectionList = getAllCollections();

        while (collectionList.hasNext()) {
            collection = collectionList.next();
            collectionName = collection.getKey();
            //TODO : exclude "roles" entity.Is there a standard list of collections we can ignore?
            if (collectionName != ROLES)
            {
                allVertices.addAll(GetEdgesForCollection(collectionName, paramsMap));
            }
        }
        return allVertices;


//        throw new UnsupportedOperationException("Not supported for Usergrid");
    }


    private List<Edge> GetEdgesForCollection(String collectionName, Map<String, Object> paramsMap) {
        List<Edge> allEdges =  new ArrayList<Edge>();
        ApiResponse responseEntities = client.apiRequest("GET", paramsMap, null, client.getOrganizationId(), client.getApplicationId(), collectionName);
        AddIntoEdgesArray(responseEntities.getEntities(), allEdges);
        while (responseEntities.getCursor() != null) {
            responseEntities = client.apiRequest("GET", paramsMap, null, client.getOrganizationId(), client.getApplicationId(), collectionName);
            AddIntoEdgesArray(responseEntities.getEntities(), allEdges);
            paramsMap.put("cursor", responseEntities.getCursor());
        }
        return allEdges;
    }

    private List<Edge> AddIntoEdgesArray(List<UsergridEntity> entities, List<Edge> allEdges) {

        Integer next = 0;
        if (entities.size() == 0) {
            return null;
        }
        while (entities.size() > next) {
            String type = entities.get(next).getType();
            String name = entities.get(next).getStringProperty("name");
            Vertex ugvertex = getVertex(type + "/" + name);
            Iterable<Edge> edges = ugvertex.getEdges(Direction.OUT);
            if (edges != null) {
                for (Edge edge : edges)
                    allEdges.add(edge);
            }
            next++;
        }
        return allEdges;
    }



    /**
     * Not implemented for Usergrid.
     * Return an iterable to all the edges in the graph that have a particular key/value property.
     *
     * @param key
     * @param value
     * @return
     */

    public Iterable<Edge> getEdges(String key, Object value) {
        throw new UnsupportedOperationException("Not supported for Usergrid");
    }


    public GraphQuery query() {
        return null;
    }


    protected void assertClientInitialized() {
        if (client == null) {
            //TODO: Initialize client? OR throw exception?
            throw new IllegalArgumentException("Client is not initialized");
        }
    }

    /**
     * Closes the client connection. Properly close the graph.
     */
    public void shutdown() {

    /*
    1. Check the client initialized.
    2. Close the connection to Usergrid.
    3. Error handling if closeConnection() failed.
    */
        assertClientInitialized();
        log.debug("DEBUG shutdown(): making the client null");
        client = null;
        //TODO : Get shutdown() of client reviewed.
    }
}
