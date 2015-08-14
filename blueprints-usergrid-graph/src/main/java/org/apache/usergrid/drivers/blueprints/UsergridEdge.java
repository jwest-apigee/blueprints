package org.apache.usergrid.drivers.blueprints;

import com.fasterxml.jackson.databind.JsonNode;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import org.apache.log4j.Logger;
import org.apache.usergrid.java.client.Usergrid;
import org.apache.usergrid.java.client.model.Connection;
import org.apache.usergrid.java.client.response.ApiResponse;
import org.springframework.http.HttpMethod;

import java.util.*;

/**
 * Created by ApigeeCorporation on 6/29/15.
 */
public class UsergridEdge extends Connection implements Edge {

  private static final Logger log = Logger.getLogger(UsergridGraph.class);


  public static final String SLASH = "/";
  public static final String STRING_UUID = "uuid";
  public static final String STRING_NAME = "name";
  public static final String STRING_DELETE = "DELETE";
  public static final String STRING_LABEL = "LABEL";
  public static final String STRING_ID = "id";
  public static final String STRING_CONNECTIONID = "connectionid";


  public UsergridEdge(String outV, String inV, String label) {
    setId(outV, label, inV);
    setLabel(label);
  }

  /**
   * This sets the label for an edge
   * @param label
   */
  public void setLabel(String label) {
    log.debug("DEBUG UsergridEdge setLabel() : Setting the label to : " + label);
    super.setLabel(label);
  }

  /**
   * Sets the property ID for the given connection as
   * soureceVertextype/sourceVertex_uuid/label/targetVertextype/targetVertex_uuid
   *  @param sourceId The ID of the source given by "soureceVertextype/sourceVertex_uuid"
   * @param label the name or label of the connection
   * @param targetId the ID of the target given by "targetVertextype/targetVertex_uuid"
   */
  private void setId(Object sourceId, String label, Object targetId) {
    assertClientInitialized();
    log.debug("DEBUG UsergridEdge setId() : Setting the Connection ID to : " + label);
    super.setConnectionID(sourceId + SLASH + label + SLASH + targetId);
  }

  /**
   * This gives the ID for the given edge in the form "sourecetype/source_uuid/label/targettype/target_uuid"
   *
   * @return Returns the String ID of the edge.
   */
  public String getId() {
    /*
    1. check if client is initialized.
    2. check if the edge is valid.
    3. return the edge id.
     */
    assertClientInitialized();
    //TODO: check if edge is valid.
    return super.getPropertyId();
  }

  /**
   * This gives the label associated with the edge in the form "SourceId/Label/TargetId"
   *
   * @return Returns the name of the edge.
   */
  public String getLabel() {
    /*
    1. get the client connection. check if its initialized.
    */
    return super.getLabel();
  }

  /**
   * This removes or deletes the edge
   */
  public void remove() {
    /*
    1. check client is initialized.
    2.check if the connection/edge is valid
    3. delete the connection . check : disconnectEntities in client.java
     */

    ValidationUtils.validateNotNull(this, IllegalArgumentException.class, "The edge specified cannot be null");
    String edgeId = this.getId();
    ValidationUtils.validateNotNull(edgeId, IllegalArgumentException.class, "The edge ID specified cannot be null");
    String[] properties = edgeId.split(SLASH);
    if(properties.length == 5) {
      String[] urlparams = {UsergridGraph.client.getOrganizationId(),UsergridGraph.client.getApplicationId(),properties[0],properties[1],properties[2],properties[3],properties[4]};
      UsergridGraph.client.apiRequest(STRING_DELETE,null,null,urlparams);
    }
    else
      log.error("The edge passed has invalid Id");
  }

  /**
   * This gives the tail/out or head/in vertex.
   *
   * @param direction of the edges to be retrieved.
   * @return the vertex
   * @throws IllegalArgumentException : throws exception if the arguments passed are invalid.
   *
   */
  public Vertex getVertex(Direction direction) throws IllegalArgumentException {
    /*
    1. Check the client initialized.
    2. check the direction : IN -> connected entity , OUT -> connecting entity //TODO discuss : what if its BOTH. ?
    3. if IN :
          get the connected Entity
                (ex : c3.getEntities().get(0).getStringProperty("name"))
      else if OUT :
          get the connecting entity
                (ex : entity1.getProperties().get("metadata").get("connecting"))
      else :
          Throw Illegal arguement exception
     */

    String edgeId = this.getId().toString();
    String type = null;
    String[] properties = ((String) edgeId).split(SLASH);
    ApiResponse response = null;
    if (direction == Direction.OUT) {
       response = UsergridGraph.client.getEntity(properties[0], properties[1]);
      type = properties[0];
      log.debug("DEBUG getVertex(): API response returned for query vertex is : " + response);
    } else if (direction == Direction.IN) {
       response = UsergridGraph.client.getEntity(properties[3], properties[4]);
      type = properties[3];
      log.debug("DEBUG getVertex(): API response returned for query vertex is : " + response);
    }
    else throw new IllegalArgumentException("Direction for getVertex for an edge cannot be BOTH");

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

    log.debug("DEBUG getVertex(): Properties of the vertex : '" + v.getProperty(STRING_NAME) + "' got are : " + v.getProperties());
    log.debug("DEBUG getVertex(): Returning vertex with uuid : " + v.getUuid().toString());
    return v; // return target vertex
  }

  /**
   * This returns the label or ID of the edge only
   *
   * @param key the property of the edge to be retrieved(label or ID).
   * @return Returns the value of the property specified by the key
   */
  public <T> T getProperty(String key) {
    if (key.toLowerCase().equals(STRING_LABEL)){
      return (T)this.getLabel();
    }
    if (key.toLowerCase().equals(STRING_ID)||key.toLowerCase().equals(STRING_CONNECTIONID)){
      return (T)this.getId();
    }
    throw new IllegalArgumentException("Property not supported");
  }

  /**
   * This gives the properties label and connectionId
   *
   * @return Returns the label and connectionId as a Set.
   */
  public Set<String> getPropertyKeys() {
    Set<String> propertyKeyList = new HashSet<String>();
    propertyKeyList.add(STRING_LABEL);
    propertyKeyList.add(STRING_CONNECTIONID);
    return  propertyKeyList;
  }

  /**
   * Not supported for usergrid
   *
   * @param key key of the property.
   * @param value value of the property.
   */
  public void setProperty(String key, Object value) {
    throw new UnsupportedOperationException("Not supported for Usergrid");
  }

  /**
   * Not supported for usergrid
   *
   * @param key  of the property to be removed.
   * @return returns null.
   */
  public <T> T removeProperty(String key) {
    throw new UnsupportedOperationException("Not supported for Usergrid");
  }

  /**
   * This compares ID of the edge to see if the edges are equal or not
   * @param obj Object to be compared to (edge)
   * @return Returns a Boolean value
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof UsergridEdge){
      UsergridEdge edge = (UsergridEdge)obj;
      boolean flag;
      if (this.getId().equals(edge.getId()))
        flag = true;
      else
        flag = false;
      return flag;

    }
    else if (obj instanceof Edge) {
      Vertex v = (Vertex) obj;
      boolean flag;
      if (this.getId().equals(v.getId()))
        flag = true;
      else
        flag = false;

      return flag;
    }
    throw new IllegalArgumentException("Couldn't compare class '" + obj.getClass() + "' with Edge");
  }

  /**
   * This returns the hashcode of the ID of the edge
   * @return Returns an integer value of the hashcode of the ID
   */
  @Override
  public int hashCode(){
    int hashCode = this.getId().hashCode();
    return hashCode;
  }

  protected void assertClientInitialized() {
    if (UsergridGraph.client == null) {
      throw new IllegalArgumentException("Client is not initialized");
    }
  }

}