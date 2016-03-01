package com.tinkerpop.blueprints.impls.usergrid;

import com.tinkerpop.blueprints.*;


import java.io.*;

/**
 * Created by ApigeeCorporation on 6/29/15.
 */
public class AppTest {

    static String filePath = "blueprints-usergrid-graph/src/main/resources/usergrid.properties";
    private static Iterable<Edge> Edges;

    public static void main(String[] args) throws FileNotFoundException {

        Graph usergrid = GraphFactory.open(filePath);

        Vertex v1 = usergrid.addVertex(null);
        Vertex v2 = usergrid.addVertex(null);
        Vertex v3 = usergrid.addVertex(null);

        Edge ev1 = usergrid.addEdge(null, v1, v2, "test1");
        Edge ev2 = usergrid.addEdge(null, v1, v2, "test2");
        Edge ev3 = usergrid.addEdge(null, v2, v1, "test3");
        Edges = v1.getEdges(Direction.BOTH);
        System.out.println("printing both incoming and outgoing edges.");

        for (Edge edge : Edges) {
            System.out.println("Edge id retrieved for vertex : " + edge.getId());
        }

        Iterable<Vertex> allvertices = usergrid.getVertices();
        for (Vertex vertex : allvertices) {
            System.out.println("inside all vertices certex id  : " + vertex.getId());
        }


        Iterable<Edge> allEdges = usergrid.getEdges();
        for (Edge edge : allEdges) {
            System.out.println("inside all edges edges id  : " + edge.getId());
        }

        System.out.println("Creating VERTICES");
        Vertex person1 = usergrid.addVertex("person/Anne");
        System.out.println("id person1 :: " + person1.getId());

        Vertex restaurant1 = usergrid.addVertex("restaurant/Amici");
        System.out.println("id restaurant1 :: " + restaurant1.getId());

        Vertex restaurant2 = usergrid.addVertex("restaurant/CPK");
        System.out.println("id restaurant2 :: " + restaurant2.getId());


        System.out.println("Checking if default object is created, when ObjectID is passed");
        Vertex object1 = usergrid.addVertex(123);
        System.out.println("id object1 :: " + object1.getId());

        System.out.println("Checking if default object is created, when random String is passed");
        Vertex object2 = usergrid.addVertex("Betty");
        System.out.println("id object1 :: " + object2.getId());


        System.out.println("Checking if default object is created, when object ID is null");
        Vertex object3 = usergrid.addVertex(null);
        System.out.println("id object3 :: " + object3.getId());

        System.out.println();
        System.out.println("Checking if object with same object UUID as that created with null object type returns the same object");
        Vertex object4 = usergrid.addVertex(object3.getId());
        Vertex object3again = usergrid.getVertex(object3.getId());
        if (object4.equals(object3again)) {
            System.out.println("Test passed");
        } else {
            System.out.println("Test failed");
            System.out.println("This is object 3:");
            System.out.println(object3again);
            System.out.println(object3again.getClass().toString());
            System.out.println("This is object 4:");
            System.out.println(object4);
            System.out.println(object4.getClass().toString());
        }

        System.out.println();
        System.out.println("Checking change of Type for a vertex with incoming and outgoing edges");
        Vertex object5 = usergrid.addVertex(null);
        object5.setProperty("name", "Object5toPerson");
        object5.addEdge("likes", restaurant1);
        restaurant2.addEdge("advertisesTo", object5);
        object5.setProperty("type", "person");
        System.out.println(object5);
        System.out.println("Has edges" + object5.getEdges(Direction.IN, "advertisesTo") + object5.getEdges(Direction.OUT, "likes"));


        System.out.println();
        System.out.println("Getting VERTICES");
        Vertex testGet = usergrid.getVertex(person1.getId()); //Gets vertex using getVetex which in turn uses getId
        System.out.println(testGet);
        Vertex testGet2 = usergrid.getVertex("person/Anne"); //Gets vertex using getVetex which in turn uses getId
        System.out.println(testGet2);

        System.out.println();
        System.out.println("Setting and getting propertices for VERTICES");
        restaurant1.setProperty("tag", "Italian"); // Sets a property
        restaurant1.setProperty("area", "MV");
        restaurant1.setProperty("rating", 5);
        Integer location = -1;
        restaurant1.setProperty("location", location);
        System.out.println("Integer " + restaurant1.getProperty("location"));
        Float number = 3.6f;
        restaurant1.setProperty("number", number);
        System.out.println("Float " + restaurant1.getProperty("number"));
        restaurant1.setProperty("exists", true);
        System.out.println("Getting the property for Amici restaurant : " + restaurant1.getProperty("tag")); //Gets a property
        System.out.println("All keys : " + restaurant1.getPropertyKeys());
        //Gets Long value
        System.out.println("Getting the rating for Amici restaurant : " + restaurant1.getProperty("rating"));
        //Gets Boolean Value
        System.out.println("Getting the rating for Amici restaurant : " + restaurant1.getProperty("exists"));


        //Removes properties
        System.out.println();
        System.out.println("Removes properties for VERTICES");
        //Long rating = restaurant1.removeEntityProperty("rating");
        String tag = restaurant1.removeProperty("tag");
        Boolean exists = restaurant1.removeProperty("exists");
        System.out.println("Properties deleted:" + " " + tag + " " + exists);


        System.out.println();
        System.out.println("Adding EDGES");
        Edge e1 = person1.addEdge("visits", restaurant1);
        System.out.println("Edge1 id : " + e1.getId());

        Edge e2 = usergrid.addEdge(null, person1, restaurant2, "visits");
        System.out.println("Edge2 id : " + e2.getId());

        Edge e3 = person1.addEdge("likes", restaurant1);
        System.out.println("Edge3 id : " + e3.getId());

        Edge e4 = restaurant1.addEdge("visitedBy", person1);
        System.out.println("Edge4 id : " + e4.getId());


        System.out.println();
        System.out.println("Getting An EDGE");

//    person:ayesha/visits/restaurant:amici
        String edgeId = person1.getId() + "/visits/" + restaurant1.getId();

        e3 = usergrid.getEdge(edgeId);
        System.out.println("Get edge : " + e3.getId());


        System.out.println("Getting outgoing EDGES");
        Iterable<Edge> edges = person1.getEdges(Direction.OUT);
        if (edges != null) {
            for (Edge each : edges) {
                System.out.println("in test app : " + each.getId());
            }
        }

        System.out.println("Getting incoming EDGES");
        Iterable<Edge> edgesIn = restaurant1.getEdges(Direction.IN);
        if (edgesIn != null) {
            for (Edge each : edgesIn) {
                System.out.println("in test app : " + each.getId());
            }
        }

        System.out.println("Getting both EDGES");
        Iterable<Edge> edgesInOut = person1.getEdges(Direction.BOTH);
        if (edgesInOut != null) {
            for (Edge each : edgesInOut) {
                System.out.println("in test app : " + each.getId());
            }
        }
        




//    System.out.println("Deleting EDGES");
//
//    usergrid.removeEdge(e3);
//    e2.remove();
//    System.out.println("Deleting VERTICES");
//    usergrid.removeVertex(restaurant1);
//    restaurant2.remove();

    }
}
