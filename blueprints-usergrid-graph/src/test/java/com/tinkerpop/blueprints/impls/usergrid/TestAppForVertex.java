package com.tinkerpop.blueprints.impls.usergrid;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphFactory;
import com.tinkerpop.blueprints.Vertex;

/**
 * Created by nishitarao on 7/7/15.
 */
public class TestAppForVertex {
    public static void main(String[] args) {

        Graph usergrid = GraphFactory.open("blueprints-usergrid-graph/src/main/resources/usergrid.properties");

        //ADD VERTEX

        //Test for null
        Vertex nullValue = usergrid.addVertex(null); // Adds a vertex

        //Test for Empty String  -- > not supported by usergrid.
//        Vertex EmptyString = usergrid.addVertex("");

        //Test for invalid String ID
        Vertex InvalidString = usergrid.addVertex("person");

        //Test for invalid Object ID
        Vertex InvalidObject = usergrid.addVertex(123);

        //Test for duplicate
        Vertex Original = usergrid.addVertex("person/Anne");
        Vertex Duplicate = usergrid.addVertex("person/Anne");

        //Check for invalid Org name
        Graph incorrectOrg_Graph = GraphFactory.open("blueprints-usergrid-graph/src/main/resources/usergrid_incorrect.properties");
        Vertex incorrectOrg_Vertex = incorrectOrg_Graph.addVertex("person/Anne");

        //Check for invalid App name
        Graph incorrectApp_Graph = GraphFactory.open("src/main/resources/usergrid_incorrectApp.properties");
        Vertex incorrectApp_Vertex = incorrectApp_Graph.addVertex("person/Anne");

        //Check for invalid credentials
        Graph incorrectCred_Graph = GraphFactory.open("src/main/resources/usergrid_incorrectCred.properties");
        Vertex incorrectCred_Vertex = incorrectCred_Graph.addVertex("person/Anne");







    }
}
