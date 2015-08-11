package org.apache.usergrid.drivers.blueprints;

import com.tinkerpop.blueprints.*;

/**
 * Created by ApigeeCorporation on 6/29/15.
 */
public class UsergridGraphQuery implements GraphQuery {

  /**
   * @param key : method not miplemented.
   * @return null
   */
  public GraphQuery has(String key) {
    return null;
  }

  /**
   * @param key : method not miplemented.
   * @return : null
   */
  public GraphQuery hasNot(String key) {
    return null;
  }

  /**
   * @param key : method not miplemented.
   * @param value : method not miplemented.
   * @return null
   */
  public GraphQuery has(String key, Object value) {
    return null;
  }

  /**
   * @param key
   * @param value
   * @return
   */
  public GraphQuery hasNot(String key, Object value) {
    return null;
  }

  /**
   * @param key : method not miplemented.
   * @param predicate : method not miplemented.
   * @param value : method not miplemented.
   * @return null
   */
  public GraphQuery has(String key, Predicate predicate, Object value) {
    return null;
  }

  /**
   * @param key
   * @param value
   * @param compare
   * @param <T>
   * @return
   */
  public <T extends Comparable<T>> GraphQuery has(String key, T value, Compare compare) {
    return null;
  }

  /**
   * @param key : method not miplemented.
   * @param startValue : method not miplemented.
   * @param endValue : method not miplemented.
   * @param <T> : method not miplemented.
   * @return null
   */
  public <T extends Comparable<?>> GraphQuery interval(String key, T startValue, T endValue) {
    return null;
  }

  /**
   * @param limit : method not miplemented.
   * @return null
   */
  public GraphQuery limit(int limit) {
    return null;
  }

  /**
   * @return null
   */
  public Iterable<Edge> edges() {
    return null;
  }

  /**
   * @return null
   */
  public Iterable<Vertex> vertices() {
    return null;
  }
}
