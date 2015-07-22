
package org.apache.usergrid.drivers.blueprints;

import com.fasterxml.jackson.databind.JsonNode;
import com.tinkerpop.blueprints.*;
import org.apache.commons.configuration.Configuration;
import org.apache.usergrid.java.client.Client;
import org.apache.usergrid.java.client.SingletonClient;
import org.apache.usergrid.java.client.response.ApiResponse;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.util.*;

/**
 * Created by ApigeeCorporation on 6/29/15.
 */
public class UsergridGraph implements Graph {

  public static final String SLASH = "/";
  public static final String STRING_UUID = "uuid";
  public static final String STRING_NAME = "name";
  public static final String ARROW_CONNECTOR = "-->";

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
    features.supportsVertexProperties = Boolean.TRUE;

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

    public static Client client;
    private String defaultType;


    /**
    * @param config
    */
    public UsergridGraph(Configuration config) {

    //TODO: Change to appropriate location
    ValidationUtils.validateNotNull(config, RuntimeException.class, "Configuration for Usergrid cannot be null" );
    this.defaultType = config.getString("usergrid.defaultType");

    //Configuration for Usergrid
    String orgName = config.getString("usergrid.organization");
    String appName = config.getString("usergrid.application");
    String apiUrl = config.getString("usergrid.apiUrl");
    String clientId = config.getString("usergrid.client_id");
    String clientSecret = config.getString("usergrid.client_secret");

    ValidationUtils.validateNotNull(orgName,RuntimeException.class,"Organization name in Usergrid cannot be null");
    ValidationUtils.validateNotNull(appName, RuntimeException.class, "Application name in Usergrid cannot be null");
    ValidationUtils.validateStringNotEmpty(orgName, RuntimeException.class, "Organization name cannot be empty in Usergrid");
    ValidationUtils.validateStringNotEmpty(appName, RuntimeException.class, "Application name cannot be empty in Usergrid");

    if (apiUrl == null)
    SingletonClient.initialize(orgName, appName);
    else
    SingletonClient.initialize(apiUrl, orgName, appName);
    //Get an instance of the client
    client = SingletonClient.getInstance();
    //Authorize the Application with the credentials provided in the Configuration file
    client.authorizeAppClient(clientId, clientSecret);

    }


    /**
    * This returns all the features that the Blueprint supports for Usergrid.
    *
    * @return
    */
    public Features getFeatures() {
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
    ValidationUtils.validateNotNull(id,RuntimeException.class,"id cannot be of type null");

    if (id instanceof String) {

        ValidationUtils.validateStringNotEmpty((String) id, RuntimeException.class, "id cannot be an empty string");

        if (id.toString().contains("/")) {
            String[] parts = id.toString().split(SLASH);
            String VertexType = parts[0];
            String VertexName = parts[1];
            UsergridVertex v = new UsergridVertex(VertexType);
            v.setLocalProperty(STRING_NAME, VertexName);
            v.setLocalProperty("_ugName", VertexName);
            v.setLocalProperty("_ugBlueprintsId", id);
            ApiResponse response = client.createEntity(v);

            ValidationUtils.serverError(response, IOException.class, "Usergrid server error");
            ValidationUtils.validateAccess(response, RuntimeException.class, "User forbidden from using the Usergrid resource");
            ValidationUtils.validateDuplicate(response, RuntimeException.class, "Entity with the name specified already exists in Usergrid");
            ValidationUtils.validateCredentials(response, RuntimeException.class, "User credentials for Usergrid are invalid");
            ValidationUtils.validateRequest(response, RuntimeException.class, "Invalid request passed to Usergrid");
            ValidationUtils.OrgAppNotFound(response, RuntimeException.class, "Organization or application does not exist in Usergrid");

            String uuid = response.getFirstEntity().getStringProperty(STRING_UUID);
            v.setUuid(UUID.fromString(uuid));
            return v;
        }
        else {
            UsergridVertex v = new UsergridVertex(defaultType);
            String VertexName = id.toString();
            v.setLocalProperty(STRING_NAME, VertexName);
            v.setLocalProperty("_ugName", VertexName);
            v.setLocalProperty("_ugBlueprintsId", id);
            ApiResponse response = client.createEntity(v);

            ValidationUtils.serverError(response, IOException.class,"Usergrid server error");
            ValidationUtils.validateAccess(response,RuntimeException.class,"User forbidden from using the Usergrid resource");
            ValidationUtils.validateDuplicate(response,RuntimeException.class, "Entity with the name specified already exists in Usergrid");
            ValidationUtils.validateCredentials(response, RuntimeException.class, "User credentials for Usergrid are invalid");
            ValidationUtils.validateRequest(response, RuntimeException.class, "Invalid request passed to Usergrid");
            ValidationUtils.OrgAppNotFound(response, RuntimeException.class, "Organization or application does not exist in Usergrid");

            String uuid = response.getFirstEntity().getStringProperty(STRING_UUID);
            v.setUuid(UUID.fromString(uuid));
            return v;
        }
    }
      else if ((id instanceof Object)){
      UsergridVertex v = new UsergridVertex(defaultType);
      String VertexName = id.toString();
      v.setLocalProperty(STRING_NAME, VertexName);
      v.setLocalProperty("_ugName", VertexName);
      v.setLocalProperty("_ugBlueprintsId", id);
      ApiResponse response = client.createEntity(v);

      ValidationUtils.serverError(response, IOException.class,"Usergrid server error");
      ValidationUtils.validateAccess(response,RuntimeException.class,"User forbidden from using the Usergrid resource");
      ValidationUtils.validateDuplicate(response,RuntimeException.class, "Entity with the name specified already exists in Usergrid");
      ValidationUtils.validateCredentials(response, RuntimeException.class, "User credentials for Usergrid are invalid");
      ValidationUtils.validateRequest(response, RuntimeException.class, "Invalid request passed to Usergrid");
      ValidationUtils.OrgAppNotFound(response, RuntimeException.class, "Organization or application does not exist in Usergrid");

      String uuid = response.getFirstEntity().getStringProperty(STRING_UUID);
      v.setUuid(UUID.fromString(uuid));
      return v;
    }
    throw new IllegalArgumentException("Supplied id class of " + String.valueOf(id.getClass()) + " is not supported by Usergrid");
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
    ValidationUtils.validateNotNull(id,RuntimeException.class,"id cannot be of type null");

    if (id instanceof String) {
        ValidationUtils.validateStringNotEmpty((String) id, RuntimeException.class, "id cannot be an empty string");

        String[] parts = id.toString().split(SLASH);
        String type = parts[0];
        String StringUUID = parts[1];
        ApiResponse response = SingletonClient.getInstance().queryEntity(type, StringUUID);

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
        }
        return v;
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
    ValidationUtils.validateNotNull(vertex,RuntimeException.class,"Vertex cannot be null");
    ValidationUtils.validateforVertex(vertex,RuntimeException.class,"Type of entity should be Vertex");
    String id = vertex.getId().toString();
    String[] parts = id.split(SLASH);
    String type = parts[0];
    String StringUUID = parts[1];
    ApiResponse response = SingletonClient.getInstance().deleteEntity(type, StringUUID);

    ValidationUtils.serverError(response, IOException.class,"Usergrid server error");
    ValidationUtils.validateAccess(response,RuntimeException.class,"User forbidden from using the Usergrid resource");
    ValidationUtils.validateCredentials(response, RuntimeException.class, "User credentials for Usergrid are invalid");
    ValidationUtils.validateRequest(response, RuntimeException.class, "Invalid request passed to Usergrid");
    ValidationUtils.OrgAppNotFound(response, RuntimeException.class, "Organization or application does not exist in Usergrid");

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
    return null;
    }

    /**
    * {
    * throw new UnsupportedOperationException("Not supported for Usergrid");
    * }
    * Returns an iterable to all the vertices in the graph.
    *
    * @return
    */


    public Iterable<Vertex> getVertices() {
    // need to be able to page
    return null;
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
    ValidationUtils.validateNotNull(label,RuntimeException.class, "Label for the edge cannot be null");
    ValidationUtils.validateStringNotEmpty(label, RuntimeException.class, "Label for the edge cannot be an empty string");

    UsergridEdge e = new UsergridEdge( outVertex.getId().toString(),  inVertex.getId().toString(), label);
    UsergridVertex source = (UsergridVertex) outVertex;
    UsergridVertex target = (UsergridVertex) inVertex;
    ApiResponse response = client.connectEntities(source, target, label);

    ValidationUtils.serverError(response, IOException.class,"Usergrid server error");
    ValidationUtils.validateAccess(response,RuntimeException.class,"User forbidden from using the Usergrid resource");
    ValidationUtils.validateDuplicate(response,RuntimeException.class, "Edge of the same type already exists between the two vertices in Usergrid");
    ValidationUtils.validateCredentials(response, RuntimeException.class, "User credentials for Usergrid are invalid");
    ValidationUtils.validateRequest(response, RuntimeException.class, "Invalid request passed to Usergrid");
    ValidationUtils.OrgAppNotFound(response, RuntimeException.class, "Organization or application does not exist in Usergrid");
    ValidationUtils.validateResourceExists(response, RuntimeException.class, "Resource does not exist in Usergrid");

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
        String[] properties = ((String) id).split(ARROW_CONNECTOR);
        String label = properties[1];
        Vertex srcVertex = getVertex(properties[0]);
        Vertex trgVertex = getVertex(properties[2]);
        Edge connection = new UsergridEdge( srcVertex.getId().toString(), trgVertex.getId().toString(), label);
        //TODO:Check if edge exists in Usergrid
        //TODO: Add validations once the check is added

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

    ValidationUtils.validateNotNull(edge,RuntimeException.class, "The edge specified cannot be null");

    String edgeId = edge.getId().toString();
    ValidationUtils.validateStringNotEmpty(edgeId, RuntimeException.class, "Unable to obtain the Edge ID of the edge specified");
    String[] properties = (edgeId).split(ARROW_CONNECTOR);
    String label = properties[1];
    UsergridVertex srcVertex = (UsergridVertex) getVertex(properties[0]);
    UsergridVertex trgVertex = (UsergridVertex) getVertex(properties[2]);
    ApiResponse response = client.disconnectEntities(srcVertex, trgVertex, label);

    ValidationUtils.serverError(response, IOException.class,"Usergrid server error");
    ValidationUtils.validateAccess(response,RuntimeException.class,"User forbidden from using the Usergrid resource");
    ValidationUtils.validateCredentials(response, RuntimeException.class, "User credentials for Usergrid are invalid");
    ValidationUtils.validateRequest(response, RuntimeException.class, "Invalid request passed to Usergrid");
    ValidationUtils.OrgAppNotFound(response, RuntimeException.class, "Organization or application does not exist in Usergrid");
    ValidationUtils.validateResourceExists(response, RuntimeException.class, "Resource does not exist in Usergrid");


    }

    /**
    * Not Implemented for Usergrid
    *
    * @return
    */

    public Iterable<Edge> getEdges() {
    throw new UnsupportedOperationException("Not supported for Usergrid");
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
    client = null;
    //TODO : Get shutdown() of client reviewed.
    }
    }
