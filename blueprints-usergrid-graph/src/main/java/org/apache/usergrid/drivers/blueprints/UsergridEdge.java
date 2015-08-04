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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by ApigeeCorporation on 6/29/15.
 */
public class UsergridEdge extends Connection implements Edge {

  private static final Logger log = Logger.getLogger(UsergridGraph.class);


  public static final String CONNECTOR = "/";

  public static final String STRING_UUID = "uuid";
  public static final String STRING_NAME = "name";


  public UsergridEdge(String outV, String inV, String label) {
    setId(outV, label, inV);
    setLabel(label);
  }

  public void setLabel(String label) {
    log.debug("DEBUG UsergridEdge setLabel() : Setting the label to : " + label );
    super.setLabel(label);
  }


  /**
   * sets the property id for the given connection as
   * <sourecetype:uuid>/<label>/<targettype:uuid>
   *  @param sourceID
   * @param label
   * @param targetId
   */
  private void setId(Object sourceID, String label, Object targetId) {
    assertClientInitialized();
    log.debug("DEBUG UsergridEdge setId() : Setting the Connection Id to : " + label );
    super.setConnectionID(sourceID + CONNECTOR + label + CONNECTOR + targetId);
  }

  /**
   * should return the Id for the given edge. <sourecetype:uuid>/<label>/<targettype:uuid>
   *
   * @return : string - the id of the edge.
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
   * Return the label associated with the edge. in the form SourceId/Label/TargetId
   *
   * @return - the name of the edge.
   */
  public String getLabel() {
    /*
    1. get the client connection. check if its initialized.
    */
    return super.getLabel();
  }


  /**
   * removes the edge
   */
  public void remove() {
    /*
    1. check client is initialized.
    2.check if the connection/edge is valid
    3. delete the connection . check : disconnectEntities in client.java
     */

    ValidationUtils.validateNotNull(this, IllegalArgumentException.class, "The edge specified cannot be null");
    String edgeId = this.getId();
    ValidationUtils.validateNotNull(edgeId, IllegalArgumentException.class, "The edge Id specified cannot be null");
    String[] properties = edgeId.split(CONNECTOR);
    if(properties.length == 5) {
//      UsergridVertex srcVertex = new UsergridVertex(properties[0]);
//      srcVertex.setUuid(UUID.fromString(properties[1]));
//      log.debug("DEBUG UsergridEdge remove() : source vertex id : " + srcVertex.getId());
//
//      UsergridVertex trgVertex = new UsergridVertex(properties[3]);
//      trgVertex.setUuid(UUID.fromString(properties[4]));
//      log.debug("DEBUG UsergridEdge remove() : target vertex id : " + trgVertex.getId());

      String[] urlparams = {UsergridGraph.client.getOrganizationId(),UsergridGraph.client.getApplicationId(),properties[0],properties[1],properties[2],properties[3],properties[4]};
      UsergridGraph.client.apiRequest("DELETE",null,null,urlparams);
    }
    else
      log.error("the edge passed has invalid Id");
  }

  /**
   * Return the tail/out or head/in vertex.
   *
   * @param direction
   * @return the vertex
   * @throws IllegalArgumentException
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
    String[] properties = ((String) edgeId).split(CONNECTOR);
    ApiResponse response = null;
    if (direction == Direction.OUT) {
       response = UsergridGraph.client.getEntity(properties[0], properties[1]);
      type = properties[0];
      log.debug("DEBUG getVertex(): Api response returned for query vertex is : " + response);
    } else if (direction == Direction.IN) {
       response = UsergridGraph.client.getEntity(properties[3], properties[4]);
      type = properties[3];
      log.debug("DEBUG getVertex(): Api response returned for query vertex is : " + response);
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

    log.debug("DEBUG getVertex(): Properties of the vertex : '" + v.getProperty("name") + "' got are : " + v.getProperties());
    log.debug("DEBUG getVertex(): Returning vertex with uuid : " + v.getUuid().toString());
      return v; // return target vertex

  }

  /**
   * Not implementing for the usergrid blueprints.
   *
   * @param key
   * @param <T>
   * @return
   */
  public <T> T getProperty(String key) {
    return null;
  }

  /**
   * Not implementing for the usergrid blueprints.
   *
   * @return
   */
  public Set<String> getPropertyKeys() {
    return null;
  }

  /**
   * Not implementing for the usergrid blueprints.
   *
   * @param key
   * @param value
   */
  public void setProperty(String key, Object value) {

  }

  /**
   * Not implementing for the usergrid blueprints.
   *
   * @param key
   * @param <T>
   * @return
   */
  public <T> T removeProperty(String key) {
    return null;
  }

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

    throw new IllegalArgumentException("Couldn't compare class '" + obj.getClass() + "'");
  }


  @Override
  public int hashCode(){
    int hashCode = this.getId().hashCode();
    return hashCode;
  }

  protected void assertClientInitialized() {
    if (UsergridGraph.client == null) {
      //TODO: Initialize client? OR throw exception?
      throw new IllegalArgumentException("Client is not initialized");
    }
  }

}