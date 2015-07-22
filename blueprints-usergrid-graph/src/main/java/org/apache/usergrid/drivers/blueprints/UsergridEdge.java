package org.apache.usergrid.drivers.blueprints;

import com.sun.javaws.exceptions.InvalidArgumentException;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import org.apache.log4j.Logger;
import org.apache.usergrid.java.client.Client;
import org.apache.usergrid.java.client.entities.*;
import org.apache.usergrid.java.client.response.ApiResponse;

import java.util.Set;
import java.util.UUID;

/**
 * Created by ApigeeCorporation on 6/29/15.
 */
public class UsergridEdge extends Connection implements Edge {

  private static final Logger log = Logger.getLogger(UsergridGraph.class);


  public static final String CONNECTOR = "/";
  public static final String COLON = ":";

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
   * @return
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
   * @return
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

    ValidationUtils.validateNotNull(this, RuntimeException.class, "The edge specified cannot be null");
    String edgeId = this.getId();
    ValidationUtils.validateNotNull(this, RuntimeException.class, "The edge Id specified cannot be null");
    String[] properties = edgeId.split(CONNECTOR);
    if(properties.length == 5) {
      UsergridVertex srcVertex = new UsergridVertex(properties[0]);
      srcVertex.setUuid(UUID.fromString(properties[1]));
      log.debug("DEBUG UsergridEdge remove() : source vertex id : " + srcVertex.getId());

      UsergridVertex trgVertex = new UsergridVertex(properties[3]);
      trgVertex.setUuid(UUID.fromString(properties[4]));
      log.debug("DEBUG UsergridEdge remove() : target vertex id : " + trgVertex.getId());

    }
    else
      log.error("the edge passed has invalid Id");
  }

  /**
   * Return the tail/out or head/in vertex.
   *
   * @param direction
   * @return
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
          //TODO : for BOTH
     */

    String edgeId = this.getId().toString();
    String[] properties = ((String) edgeId).split(CONNECTOR);
    String[] source = properties[0].split(COLON);
    String[] target = properties[2].split(COLON);

    switch (direction) {
      case OUT:
        UsergridVertex srcVertex = new UsergridVertex(source[0]);
        srcVertex.setUuid(UUID.fromString(source[1]));
        return srcVertex; //return source vertex
      case IN:
        UsergridVertex trgVertex = new UsergridVertex(target[0]);
        trgVertex.setUuid(UUID.fromString(target[1]));
        return trgVertex;  // return target vertex
    }

    return null;
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


  protected void assertClientInitialized() {
    if (UsergridGraph.client == null) {
      //TODO: Initialize client? OR throw exception?
      throw new IllegalArgumentException("Client is not initialized");
    }
  }

}