
package org.apache.usergrid.drivers.blueprints;

import com.fasterxml.jackson.databind.JsonNode;
import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.util.DefaultGraphQuery;
import org.apache.commons.configuration.Configuration;
import org.apache.usergrid.java.client.Usergrid;
import org.apache.usergrid.java.client.UsergridClient;
import org.apache.usergrid.java.client.model.UsergridEntity;
import org.apache.usergrid.java.client.query.LegacyQueryResult;
import org.apache.usergrid.java.client.query.UsergridQuery;
import org.apache.usergrid.java.client.response.UsergridResponse;

import java.io.IOException;
import java.util.*;
import org.apache.log4j.Logger;

import javax.ws.rs.NotAuthorizedException;

/**
 * Created by ApigeeCorporation on 6/29/15.
 * A Usergrid Graph is a container object for a collection of vertices and a collection edges.
 */
public class UsergridGraph implements Graph {

    private static final int COUNT = 0;
    private static final String LIMIT = "limit";
    private static final String EVENTS = "events" ;
    private static final String HTTP_GET = "GET";
    private static final String STRING_CURSOR = "cursor";
    private static final String ACTIVITIES = "activities";
    private static final String USERS = "users";
    private static final String ASSETS = "assets";
    private static final String NULLS = "nulls";
    private static final String FOLDERS = "folders";


    public static UsergridClient client;
    private static String defaultType;
    private static int entityRetrivalCount;

    ArrayList<String> ignoreList = new ArrayList<String>();
    Iterable<Edge> edges;
    Iterable<Vertex> vertcies;

    private static String METADATA = "metadata";
    private static String COLLECTIONS = "collections";
    private static String ROLES = "roles";
    private static final Logger log = Logger.getLogger(UsergridGraph.class);

    public static final String SLASH = "/";
    public static final String STRING_UUID = "uuid";
    public static final String STRING_NAME = "name";
    public static final String STRING_TYPE = "type";
    public static final String UNAUTHORIZED = "Unauthorized";

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
        features.supportsSelfLoops = Boolean.TRUE;

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
        features.supportsEdgeIteration = Boolean.TRUE;

        /**
         * Does the graph support graph.getVertices()?
         */
        features.supportsVertexIteration = Boolean.TRUE;

        /**
         * Does the graph support retrieving edges by id, i.e. graph.getEdge(Object id)?
         */
        features.supportsEdgeRetrieval = Boolean.TRUE;

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
        ValidationUtils.validateNotNull(config, IllegalArgumentException.class, "Configuration for Usergrid cannot be null");
        this.defaultType = config.getString("usergrid.defaultType");
        String retrivalCount = config.getString("usergrid.entityRetrivalCount");
        this.entityRetrivalCount = Integer.parseInt(retrivalCount);
                log.debug("UsergridGraph() : Setting the default type to : " + this.defaultType);

        //Configuration for Usergrid
        String orgName = config.getString("usergrid.organization");
        String appName = config.getString("usergrid.application");
        String apiUrl = config.getString("usergrid.apiUrl");
        String clientId = config.getString("usergrid.client_id");
        String clientSecret = config.getString("usergrid.client_secret");

        ignoreList.add(ROLES);
        ignoreList.add(EVENTS);
        ignoreList.add(ACTIVITIES);
        ignoreList.add(USERS);
        ignoreList.add(ASSETS);
        ignoreList.add(NULLS);
        ignoreList.add(FOLDERS);

        ValidationUtils.validateNotNull(orgName, IllegalArgumentException.class, "Organization name in Usergrid cannot be null");
        ValidationUtils.validateNotNull(appName, IllegalArgumentException.class, "Application name in Usergrid cannot be null");
        ValidationUtils.validateStringNotEmpty(orgName, RuntimeException.class, "Organization name cannot be empty in Usergrid");
        ValidationUtils.validateStringNotEmpty(appName, RuntimeException.class, "Application name cannot be empty in Usergrid");

        try {
            if (apiUrl == null)
                Usergrid.initSharedInstance(apiUrl, orgName, appName);
            else
                Usergrid.initSharedInstance(apiUrl, orgName, appName);
            log.debug("UsergridGraph() : Initializing the SingletonClient");
        }
        catch (Exception e){
            System.out.println( "caught the exception : " + e);
        }

        //Get an instance of the client
        client = Usergrid.getInstance();
        ValidationUtils.validateNotNull(client, IllegalArgumentException.class, "Client could not be instantiated.");

        //Authorize the Application with the credentials provided in the Configuration file
        client.authenticateApp(clientId, clientSecret);
        log.debug("UsergridGraph() : Authorizing the client application. Client is initialized with the application url : " + client.getBaseUrl() + client.getOrgId());

       // edges = this.getEdges();
        //vertcies = this.getVertices();
    }


    /**
     * This returns all the features that the Blueprints supports for Usergrid.
     *
     * @return Returns all the features that the Blueprint supports for Usergrid.
     */
    public Features getFeatures() {
        log.debug("getFeatures() : The features set are : " + features);
        return features;
    }


    /**
     * This creates a new vertex of type default, unless specified by passing type in the ID "type/name".
     * If the vertex withe  particular ID already exists, it returns that vertex.
     * @param id - ID is of type object and can be null
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
        if (id instanceof String|| id instanceof Object) {
            log.debug("DEBUG addVertex(): id passed is an instance of String or Object");
            String StringID = id.toString();
            //Check for empty string passed
            if (id instanceof String) {
                ValidationUtils.validateStringNotEmpty((String) id, RuntimeException.class, "ID cannot be an empty string");
            }
            //Check if the string has a Slash in it to check if type is specified
            if (StringID.contains(SLASH)) {
                try {
                    Vertex vertex = this.getVertex(StringID);
                    if (vertex != null)
                        return vertex;
                }
                catch(NotAuthorizedException e){
                }
                parts = StringID.split(SLASH);
                VertexType = parts[0];
                VertexName = parts[1];
                v = new UsergridVertex(VertexType);
                v.setLocalProperty(STRING_NAME, VertexName);
                v.setLocalProperty("_ugName", VertexName);
                v.setLocalProperty("_ugBlueprintsId", id);

            }
            else{
                try {
                    Vertex vertex = this.getVertex(defaultType + SLASH + StringID);
                    if (vertex != null)
                        return vertex;
                }
                catch(NotAuthorizedException e){}

                v = new UsergridVertex(defaultType);
                VertexName = StringID;
                v.setLocalProperty(STRING_NAME, VertexName);
                v.setLocalProperty("_ugName", VertexName);
                v.setLocalProperty("_ugBlueprintsId", id);
            }

        }
        else if (id == null){
        v = new UsergridVertex(defaultType);
        }
        else
        {
        log.error("ERROR addVertex(): ID passed is in an invalid format.");
        throw new IllegalArgumentException("Supplied id class of " + String.valueOf(id.getClass()) + " is not supported by Usergrid");
        }


        UsergridResponse response = client.createEntity(v);
        UsergridEntity cVertex = response.first();
        if (cVertex.getName() == null){
            cVertex.putproperty("name",cVertex.getUuidString());
            cVertex.save();
        }

        log.debug("DEBUG addVertex(): Api response returned for adding vertex is : " + response);
        ValidateResponseErrors(response);
        ValidationUtils.validateDuplicate(response, RuntimeException.class, "Entity with the name specified already exists in Usergrid");

        Vertex vFormatted = CreateVertexFromEntity(cVertex);
        log.debug("DEBUG addVertex(): Returning vertex with uuid : " + vFormatted.getId().toString());
        return vFormatted;

    }


    /**
     * This gets a particular Vertex (entity) using the ID of the vertex.
     *
     * @param id : ID of the vertex to retrieve. The ID is in the form of "type/UUID" or "type/name"
     * @return returns the vertex with specified ID and given type.
     */
    public Vertex getVertex(Object id) {
    /*
    1) Check if client is initialized
    2) Check that ID is of supported type, else throw IllegalArgumentException error
    3) Get and return the entity - Query queryEntitiesRequest(HttpMethod method,Map<String,
    Object> params, Object data, String... segments) in org.apache.usergrid.java.client
    4) Return null if no vertex is referenced by the identifier
    */

        assertClientInitialized();
        ValidationUtils.validateNotNull(id, IllegalArgumentException.class, "id cannot be of type null");

        if (id instanceof String) {
            log.debug("DEBUG getVertex(): ID is an instance of sting");
            ValidationUtils.validateStringNotEmpty((String) id, RuntimeException.class, "ID cannot be an empty string");
            String type;
            String StringUUID;
            String qString;
            if (((String) id).contains(SLASH)) {
                String[] parts = id.toString().split(SLASH);
                type = parts[0];
                StringUUID = parts[1];


            } else{
                type = defaultType;
                StringUUID = id.toString();

            }

            qString = "select * where name = " + StringUUID + " or uuid = " + StringUUID;
            HashMap<String,Object> param = new HashMap<String, Object>();
            param.put("ql",qString);
            UsergridResponse response = client.apiRequest("GET",param,null,client.getOrgId(),client.getAppId(),type);
            if(response.first() == null)
                return null;
            log.debug("DEBUG getVertex(): Api response returned for query vertex is : " + response);
//            ValidateResponseErrors(response.first());

            UsergridVertex ugvertex = CreateVertexFromEntity(response.first());

            log.debug("DEBUG getVertex(): Properties of the vertex : '" + ugvertex.getProperty(STRING_NAME) + "' got are : " + ugvertex.getProperties());
            log.debug("DEBUG getVertex(): Returning vertex with uuid : " + ugvertex.getUuid().toString());
            return ugvertex;

        }
        throw new IllegalArgumentException("Supplied ID class of " + String.valueOf(id.getClass()) + " is not supported by Usergrid");

    }

    /**
     * This deletes a particular vertex (entity) by taking the vertex as an identifier
     *
     * @param vertex : vertex to be removed.
     */
    public void removeVertex(Vertex vertex) {

    /*
    1) Check if client is initialized
    2) Check if vertex exists
    3) Delete all edges connected to the vertex using disconnect(String connectingEntityType,
    String connectingEntityId, String connectionType, String connectedEntityId) in org.apache.usergrid.java.client
    4) Delete the vertex //TODO: The method delete() is defined in org.apache.usergrid.java.client.entities but has not been implemented
    5) Return null if no vertex is referenced by the identifier
    */
        assertClientInitialized();
        ValidationUtils.validateNotNull(vertex, IllegalArgumentException.class, "Vertex cannot be null");
        ValidationUtils.validateforVertex(vertex, RuntimeException.class, "Type of entity should be Vertex");
        String id = vertex.getId().toString();
        String[] parts = id.split(SLASH);
        String type = parts[0];
        String StringUUID = parts[1];

        try {
            UsergridResponse response = client.deleteEntity(type, StringUUID);
        }
        catch(NotAuthorizedException e){
            throw new IllegalStateException("Vertex you are trying to delete does not exist");
        }

    }

    public static void ValidateResponseErrors(UsergridResponse response) {
        ValidationUtils.serverError(response, IOException.class, "Usergrid server error");
        ValidationUtils.validateAccess(response, RuntimeException.class, "User forbidden from using the Usergrid resource");
        ValidationUtils.validateCredentials(response, RuntimeException.class, "User credentials for Usergrid are invalid");
        ValidationUtils.validateRequest(response, RuntimeException.class, "Invalid request passed to Usergrid");
        ValidationUtils.OrgAppNotFound(response, RuntimeException.class, "Organization or application does not exist in Usergrid");
    }

    /**
     * Unsupported for Usergrid
     * Return an iterable to all the vertices in the graph that have a particular key/value property.
     *
     * @param key : unsupported method
     * @param value : unsupported method
     * @return : unsupported method
     */
    public Iterable<Vertex> getVertices(String key, Object value) {
        throw new UnsupportedOperationException("Not Supported in Usergrid");
    }


    /**
     * Returns an iterable to all the vertices in the graph for all collection types.
     *
     * @return : iterable to all the vertices in the graph
     */
    public Iterable<Vertex> getVertices() {
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put(LIMIT, entityRetrivalCount);
        List<Vertex> allVertices = new ArrayList<Vertex>();
        Iterator<Map.Entry<String, JsonNode>> collectionList = getAllCollections();
        while (collectionList.hasNext()) {
            Map.Entry<String, JsonNode> collection = collectionList.next();
            String collectionName = collection.getKey();
            if (! ignoreList.contains(collectionName)) {
                allVertices.addAll(GetVerticesForCollection(collectionName, paramsMap));
            }
        }
        return allVertices;
    }

    private List<Vertex> GetVerticesForCollection(String collectionName, Map<String, Object> paramsMap) {
        List<Vertex> allVertices =  new ArrayList<Vertex>();
        UsergridResponse responseEntities = client.apiRequest(HTTP_GET, paramsMap, null, client.getOrgId(), client.getAppId(), collectionName);
        ValidateResponseErrors(responseEntities);
       if (responseEntities.getEntities().size() != 0) {
            AddIntoEntitiesArray(responseEntities.getEntities(), allVertices);
            while (responseEntities.getCursor() != null) {
                paramsMap.put(STRING_CURSOR, responseEntities.getCursor());
                responseEntities = client.apiRequest(HTTP_GET, paramsMap, null, client.getOrgId(), client.getAppId(), collectionName);
                ValidateResponseErrors(responseEntities);
                AddIntoEntitiesArray(responseEntities.getEntities(), allVertices);
            }
        }
        return allVertices;
    }

    private Iterator<Map.Entry<String, JsonNode>> getAllCollections() {
        UsergridResponse response = client.queryCollections();
        return response.getFirstEntity().getProperties().get(METADATA).get(COLLECTIONS).fields();
    }

    private List<Vertex> AddIntoEntitiesArray(List<UsergridEntity> entities, List<Vertex> allVertices) {
        Integer next = 0;
        if (entities.size() == 0) {
            return new ArrayList<Vertex>();
        }
        while (entities.size() > next) {
            Vertex ugvertex = CreateVertexFromEntity(entities.get(next));
            allVertices.add(ugvertex);
            next++;
        }
        return allVertices;
    }

    public static UsergridVertex CreateVertexFromEntity(UsergridEntity entity) {
        String type = entity.getType();
        UUID uuid = entity.getUuid();
        Map<String, JsonNode> vertexProperties = new HashMap<String, JsonNode>();
        vertexProperties = entity.getProperties();
        UsergridVertex ugvertex = new UsergridVertex(type);
        ugvertex.setUuid(uuid);
        for (Map.Entry<String, JsonNode> entry : vertexProperties.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            ugvertex.setLocalProperty(key, value);
        }
        return ugvertex;
    }


    /**
     * This function adds a connection (or an edge) between two vertices
     *
     * @param id : ID if the edge.
     * @param outVertex : source Vertex.
     * @param inVertex : target Vertex.
     * @param label : name or label of the edge.
     * @return : newly formed edge.
     */
    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {

    /*
    1. Check client initialized.
    2. Check if the two vertices are valid.
    3. Retrieve the EntityIds of the two entities
    3. Call connect( String connectingEntityType, String connectingEntityId, String connectionType, String connectedEntityId)
    4. Return the connection(or edge) // TODO : currently returns UsergridResponse. Should return an edge.
    */

        assertClientInitialized();
        ValidationUtils.validateNotNull(outVertex, IllegalArgumentException.class, "The vertex specified from where the Edge starts cannot be null");
        ValidationUtils.validateNotNull(inVertex, IllegalArgumentException.class, "The vertex specified, where the Edge ends cannot be null");
        ValidationUtils.validateNotNull(label, IllegalArgumentException.class, "Label for the edge cannot be null");
        ValidationUtils.validateStringNotEmpty(label, RuntimeException.class, "Label for the edge cannot be an empty string");

        UsergridEdge e = new UsergridEdge(outVertex.getId().toString(), inVertex.getId().toString(), label);
        UsergridVertex source = (UsergridVertex) outVertex;
        UsergridVertex target = (UsergridVertex) inVertex;
        UsergridResponse response = client.connect(source, label,target);
        log.debug("DEBUG addEdge(): Api response returned after add edge is : " + response);

        return e;

    }

    /**
     * This function returns a connection (or edge). Takes the Connection ID as an input which is specified as
     * SourceVertexId/connection/TargetVertexId
     *
     * @param id : ID of the edge to be retrieved.
     * @return : the edge whose ID is passed.
     */
    public Edge getEdge(Object id) {

    /*
    1. Get the client. Check if client initialzed.
    2. Get the source vertex.
    3. Get the target vertex.
    4. Return the connection(or edge).
    */
        assertClientInitialized();
        ValidationUtils.validateNotNull(id, IllegalArgumentException.class, "ID specified cannot be of type null");

        if (id instanceof String) {
            ValidationUtils.validateStringNotEmpty(id.toString(), RuntimeException.class, "ID cannot be an empty string");
            String[] properties = ((String) id).split(SLASH);
            if(properties.length != 5) {
                log.error("Object ID passed is invalid");
                return null;
            }
            String label = properties[2];

            //Check if the edge is valid.
            UsergridResponse response = client.apiRequest(HTTP_GET, null, null, client.getOrgId(), client.getAppId(), id.toString());
            if (response.getError() != null) {
//                log.error("The get requested does not exists in the database.");
                throw new RuntimeException("The Edge requested does not exists in the database. Quitting... ");
            }

            Vertex srcVertex = getVertex(properties[0] + SLASH + properties[1]);
            log.debug("DEBUG getEdge(): source vertex returned with id : " + srcVertex.getId());

            Vertex trgVertex = getVertex(properties[3] + SLASH + properties[4]);
            log.debug("DEBUG getEdge(): target vertex returned with id : " + trgVertex.getId());

            client.queryConnection(properties);
            Edge connection = new UsergridEdge(srcVertex.getId().toString(), trgVertex.getId().toString(), label);
            log.debug("DEBUG addEdge(): Returning Edge with ID : " + connection.getId());

            return connection;
        }

        log.error("Supplied ID class of " + String.valueOf(id.getClass()) + " is not supported by Usergrid");
        return  null;
    }


    /**
     * This function removes the connection between two entities in the graph. Takes the Connection
     * ID as an input which is specified as SourceVertexId/connection/TargetVertexId
     *
     * @param edge : edge to remove.
     */
    public void removeEdge(Edge edge) {

    /*
    1. Get the client. Check if its intitialzed.
    2. Get the connection(or edge) by the ID
    3. Check if the edge is a valid edge.
    4. call disconnect(String connectingEntityType, String connectingEntityId, String connectionType, String connectedEntityId)
    */

        assertClientInitialized();

        ValidationUtils.validateNotNull(edge, IllegalArgumentException.class, "The edge specified cannot be null");

        String edgeId = edge.getId().toString();
        ValidationUtils.validateStringNotEmpty(edgeId, RuntimeException.class, "Unable to obtain the Edge ID of the edge specified");
        String[] properties = (edgeId).split(SLASH);
        String label = properties[2];
        UsergridVertex srcVertex = (UsergridVertex) getVertex(properties[0] + SLASH + properties[1]);
        UsergridVertex trgVertex = (UsergridVertex) getVertex(properties[3] + SLASH + properties[4]);
        log.debug("DEBUG getvertEdge(): source vertex returned with ID : " + srcVertex.getId());

        UsergridResponse response = client.disconnect(srcVertex, label, trgVertex);
        log.debug("DEBUG removeEdge(): Response returned from API call to disconnect Vertices is : " + response);

        ValidateResponseErrors(response);
        ValidationUtils.validateResourceExists(response, RuntimeException.class, "Resource does not exist in Usergrid");

        log.debug("DEBUG removeEdge(): exiting from remove edge.");
    }

    /**
     * Returns all the edges in the graph.
     * @return : all the edges in the graph.
     */

    public Iterable<Edge> getEdges() {
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put(LIMIT, entityRetrivalCount);
        List<Edge> allEdges = new ArrayList<Edge>();
        Iterator<Map.Entry<String, JsonNode>> collectionList;
        collectionList = getAllCollections();
        while (collectionList.hasNext()) {
            Map.Entry<String, JsonNode> collection = collectionList.next();
            String collectionName = collection.getKey();
            if (! ignoreList.contains(collectionName))
                allEdges.addAll(GetEdgesForCollection(collectionName, paramsMap));
        }
        return allEdges;
    }


    private List<Edge> GetEdgesForCollection(String collectionName, Map<String, Object> paramsMap) {
        List<Edge> allEdges =  new ArrayList<Edge>();
        UsergridResponse responseEntities = client.apiRequest(HTTP_GET, paramsMap, null, client.getOrgId(), client.getAppId(), collectionName);
        AddIntoEdgesArray(responseEntities.getEntities(), allEdges);
        while (responseEntities.getCursor() != null) {
            paramsMap.put(STRING_CURSOR, responseEntities.getCursor());
            responseEntities = client.apiRequest(HTTP_GET, paramsMap, null, client.getOrgId(), client.getAppId(), collectionName);
            AddIntoEdgesArray(responseEntities.getEntities(), allEdges);
        }
        return allEdges;
    }

    private List<Edge> AddIntoEdgesArray(List<UsergridEntity> entities, List<Edge> allEdges) {

        Integer next = 0;
        if (entities.size() == 0) {
            return new ArrayList<Edge>();
        }
        while (entities.size() > next) {
            Vertex ugvertex = CreateVertexFromEntity(entities.get(next));
            Iterable<Edge> edges = ugvertex.getEdges(Direction.OUT);
            if (edges != null) {
                for (Edge edge : edges)
                    if(!allEdges.contains(edge))
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
     * @param key : Not implemented
     * @param value : Not implemented
     * @return : unsuported method.
     */

    public Iterable<Edge> getEdges(String key, Object value) {
        throw new UnsupportedOperationException("Not supported for Usergrid");
    }


    /**
     * Helps query the graph for vertices or edges usig their properties
     * @return returns the result of the GraphQuery
     */
    public GraphQuery query() {
        return new DefaultGraphQuery(this);
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
        Iterable<Vertex> vertices = this.getVertices();
        for (Vertex vertex : vertices){
            this.removeVertex(vertex);
        }
        log.debug("DEBUG shutdown(): making the client null");
        client = null;
    }

    /**
     * This helps get the simple class name of the graph
     *
     * @return Returns the simple graph name
     */
    @Override
    public String toString(){
        return getClass().getSimpleName().toLowerCase();
    }
}
