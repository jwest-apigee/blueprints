package org.apache.usergrid.drivers.blueprints;

import com.tinkerpop.blueprints.Vertex;
import org.apache.usergrid.java.client.response.UsergridResponse;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by ApigeeCorporation on 7/10/15.
 */
public class ValidationUtils {

  public static final String STRING_DUPLICATE_PROPERTY = "duplicate_unique_property_exists";
  //TODO: Enter the appropriate error string
  public static final String INVALID_CREDENTIALS = "auth_unverified_oath";
  public static final String FORBIDDEN = "forbidden";
  public static final String SERVER_ERROR = "Internal Server Error";
  public static final String INCORRECT_CONTENT = "illegal_argument";
  public static final String ORG_APP_NOT_FOUND= "organization_application_not_found";
  public static final String RESOURCE_NOT_FOUND = "service_resource_not_found";

  public static void validateNotNull(Object o, Class<IllegalArgumentException> exceptionClass, String message) {
    if (o == null) {
      try {
        Constructor<IllegalArgumentException> c = exceptionClass.getDeclaredConstructor(String.class);
        RuntimeException e = c.newInstance(message);
        throw e;
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }


  public static void validateStringNotEmpty(String s, Class<RuntimeException> exceptionClass, String message) {
    if (s == null || s.length() == 0) {

      try {
        Constructor<RuntimeException> c = exceptionClass.getDeclaredConstructor(String.class);
        RuntimeException e = c.newInstance(message);
        throw e;
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }


    }
  }

  public static void validateforVertex(Object o, Class<RuntimeException> exceptionClass, String message){
    if (!(o instanceof Vertex)){
      try {
        Constructor<RuntimeException> c = exceptionClass.getDeclaredConstructor(String.class);
        RuntimeException e = c.newInstance(message);
        throw e;
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }

  public static void validateDuplicate(UsergridResponse response, Class<RuntimeException> exceptionClass, String message) {
    if (response.toString().contains(STRING_DUPLICATE_PROPERTY)) {
      try {
        Constructor<RuntimeException> c = exceptionClass.getDeclaredConstructor(String.class);
        RuntimeException e = c.newInstance(message);
        throw e;
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }

    }
  }

  public static void validateCredentials(UsergridResponse response, Class<RuntimeException> exceptionClass, String message){
    if (response.toString().contains(INVALID_CREDENTIALS)){
      try {
        Constructor<RuntimeException> c = exceptionClass.getDeclaredConstructor(String.class);
        RuntimeException e = c.newInstance(message);
        throw e;
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }

  public static void OrgAppNotFound(UsergridResponse response, Class<RuntimeException> exceptionClass, String message) {
    if (response.toString().contains(ORG_APP_NOT_FOUND)) {

      try {
        Constructor<RuntimeException> c = exceptionClass.getDeclaredConstructor(String.class);
        RuntimeException e = c.newInstance(message);
        throw e;
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static void validateAccess(UsergridResponse response, Class<RuntimeException> exceptionClass, String message){
    if (response.toString().contains(FORBIDDEN)){
      try {
        Constructor<RuntimeException> c = exceptionClass.getDeclaredConstructor(String.class);
        RuntimeException e = c.newInstance(message);
        throw e;
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }

    public static void validateRequest(UsergridResponse response, Class<RuntimeException>exceptionClass, String message) {
    if (response.toString().contains(INCORRECT_CONTENT)){
        try {
            Constructor<RuntimeException> c = exceptionClass.getDeclaredConstructor(String.class);
            RuntimeException e = c.newInstance(message);
            throw e;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    }

    public static void validateResourceExists(UsergridResponse response, Class<RuntimeException>exceptionClass, String message){
        if (response.toString().contains(RESOURCE_NOT_FOUND)){
            try {
                Constructor<RuntimeException> c = exceptionClass.getDeclaredConstructor(String.class);
                RuntimeException e = c.newInstance(message);
                throw e;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


  public static void serverError(UsergridResponse response, Class<IOException> exceptionClass, String message){
    if (response.toString().contains(SERVER_ERROR)){
      try {
        Constructor<IOException> c = exceptionClass.getDeclaredConstructor(String.class);
        IOException e = c.newInstance(message);
        throw e;
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}