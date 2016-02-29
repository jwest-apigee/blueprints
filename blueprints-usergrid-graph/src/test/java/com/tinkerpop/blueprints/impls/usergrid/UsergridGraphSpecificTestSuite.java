package com.tinkerpop.blueprints.impls.usergrid;

import com.fasterxml.jackson.databind.JsonNode;
import com.tinkerpop.blueprints.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.usergrid.drivers.blueprints.UsergridGraph;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * Created by ayeshadastagiri on 7/16/15.
 */
public class UsergridGraphSpecificTestSuite extends TestSuite {
    private String graphProperties = "/var/lib/jenkins/workspace/usergrid-blueprints/blueprints-usergrid-graph/src/main/resources/usergrid.properties";
    private Graph graph;
    private String defaultType;

    @Override
    protected void setUp() throws Exception {
        System.out.println("Setting it up!");
        graph = getGraph();
    }

    public Graph getGraph() {
        return generateGraph(graphProperties);
    }

    public Graph generateGraph(String s) {
        PropertiesConfiguration conf = null;
        try {
            conf = new PropertiesConfiguration(s);
            defaultType = conf.getString("usergrid.defaultType");
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        UsergridGraph graph = new UsergridGraph(conf);
        return graph;
    }


    public void testBasicUgAddVertex1() {
        //Case1 : Vertex with valid id

        Vertex vertex = graph.addVertex("person/personV1");
        //validate the id is set correct.
        assertEquals("person/personV1", vertex.getId().toString());



        //Case2 : Adding vertex with default type
        vertex = graph.addVertex("personV2");
        assertEquals(defaultType + "/personV2", vertex.getId().toString());

        //Case3 : Changing the type of the vertex
        //TODO : once set type is implemented will uncomment this code.
      //  vertex.putproperty("type", "person");
        //assertEquals("person/personV2", vertex.getId().toString());
    }

    public void testBasicUgAddVertex2() {
      //case4 : adding a null vertex.
        Vertex vertex = graph.addVertex(null);
        assertNotNull(vertex.getId().toString());

        //case5 : adding null as a string for vertex name.
        vertex = graph.addVertex("null/null");
        assertEquals("null/null", vertex.getId().toString());

        //case6 : adding a vertex with Object.
        Object obj =  40;
        vertex = graph.addVertex(obj);
        assertNotNull(vertex.getId().toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentException(){
        //adding float as vertex name. should throw not supported exception
        try {
            graph.addVertex(23.9);
            fail();
            JSONObject json = new JSONObject();
            json.append("name","Mary");
            json.append("type","person");
            graph.addVertex(json);
            fail();
        }
        catch(Exception exception){
            System.out.println("exception : " + exception);
        }

    }

    //test run time exception
//        vertex = graph.addVertex("");
//        assertNotNull(vertex.getId().toString());

    public void testBasicUgSetVertexProperties() {
        //Case1 : Vertex with valid id and property
        Vertex v = graph.addVertex("person/personV3");

            //TODO: failing , shoudl check.
//        v.putproperty("age",20);
//        assertEquals(Integer.parseInt("20"), v.getProperty("age"));

        //setting multiple properties - float and string
        v.setProperty("weight", Float.parseFloat("97.5"));
        assertEquals(Float.parseFloat("97.5"), v.getProperty("weight"));

        //
        v.setProperty("city","SanJose");
        assertEquals("SanJose", v.getProperty("city"));

        //setting null property
        v.setProperty("zipcode",null);
        assertEquals(null, v.getProperty("zipcode"));

    }

    public void testGetEdgeforVertex(){
        Vertex v1 = graph.addVertex("person/personV4");
        v1.setProperty("age",20);
        Vertex v2 = graph.addVertex("person/personV5");

        Edge e = v1.addEdge("likes",v2);
        Iterable<Edge> e1 = v1.getEdges(Direction.OUT,"likes");

        assertEquals(e.getId(),e1.iterator().next().getId());

    }


    public void testBasicUgAddEdge1() {
        //Case1 : Edge with valid id
        Vertex v1 = graph.addVertex("person/personE1");
        Vertex v2 = graph.addVertex("restaurants/Amici");

        Edge e1 = graph.addEdge(null, v1, v2, "likes");
        assertEquals(v1.getId() + "/likes/" + v2.getId(), e1.getId().toString());

        //Case2 : Edge with id -- should not throw errors.
        Edge e2 = graph.addEdge("id1", v1, v2, "likes");
        assertEquals(v1.getId() + "/likes/" + v2.getId(), e2.getId().toString());

        //case3 :Adding edge from a vertex.
        Edge e3 = v1.addEdge("visits", v2);
        assertEquals(v1.getId() + "/visits/" + v2.getId(), e3.getId().toString());
    }
    public void testBasicUgAddEdge2() {

        Vertex v1 = graph.addVertex("person/personE1");
        Vertex v2 = graph.addVertex("restaurants/Amici");

        //case4 :adding random edge with special characters
        Edge e4 = graph.addEdge(null, v1,v2,"suggests-1?");
        assertEquals(v1.getId()+"/suggests-1?/"+v2.getId(),e4.getId().toString());

        //case5 : adding numbers as edge name.
        Edge e5 = graph.addEdge(null, v1,v2, String.valueOf(40));
        assertEquals(v1.getId()+"/40/"+v2.getId(),e5.getId().toString());

        //case6 : adding id while creating an edge should be ignored. Should not throw errors.
        Edge e6 = graph.addEdge(100, v1,v2, String.valueOf(30.3));
        assertNotNull(e6.getId());

    }

    public void testUgEdge() {
        Vertex v1 = graph.addVertex("person/personE2");
        Vertex v2 = graph.addVertex("restaurant/CheeseCakeFactory");

        Edge e4 = graph.addEdge(null, v1,v2,"likes");
        assertEquals(v1.getId()+"/likes/"+v2.getId(),e4.getId().toString());
        assertEquals("person/personE2",e4.getVertex(Direction.OUT).getId());
        assertEquals("restaurant/CheeseCakeFactory",e4.getVertex(Direction.IN).getId());
        assertEquals("likes",e4.getLabel());
    }


    @Override
    protected void tearDown() throws Exception {
        System.out.println("Running: tearDown");
//        graph.shutdown();
        graph = null;
        assertNull(graph);
    }


}
