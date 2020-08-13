package com.anton22255.graph

import org.graphstream.algorithm.generator.*
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.graph.Node
import org.graphstream.graph.Graph
import org.graphstream.graph.Edge
//import org.graphstream.stream.file.FileSinkImages.Resolutions
//import org.graphstream.stream.file.FileSinkImages

class GraphVisualisation {

}

fun graphExplore(peers: List<com.anton22255.agent.Agent>) {
    System.setProperty("org.graphstream.ui", "swing");
    val graph = SingleGraph("tutorial 1")
//    val pic = FileSinkImages(FileSinkImages.OutputType.PNG, Resolutions.HD1080)
//
//    pic.setLayoutPolicy(FileSinkImages.LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);
    graph.isStrict = false
    graph.setAutoCreate(true)
    graph.display()
    peers.forEach { peer ->
        peer.channels.forEach {
            graph.addEdge("${peer.id}${it.id}", peer.id, it.id, false)
        }
    }
////
    for (node in graph) {
        node.setAttribute("ui.label", node.id)
    }
//
//    pic.writeAll(graph, "sample.png");
    graph.setAttribute("ui.screenshot", "screenshot.png");

//        val viewer = graph.display(false)
//        val view = viewer.defaultView
//        view.resizeFrame(800, 600)
//        view.setViewCenter(440000, 2503000, 0)
//        view.setViewPercent(0.25)
}


//fun generateGraph(count: Int, name: String, type: GraphType? = null): SingleGraph {
//    val graph = SingleGraph("Barabasi-Albert")
//    // Between 1 and 3 new links per node added.
//    val gen = type?.createGenerator() ?: DorogovtsevMendesGenerator();
//    // Generate 100 nodes:
//    gen.addSink(graph)
//    gen.begin()
//
//    val nodes = if (type == GraphType.Grid) Math.sqrt(count.toDouble()).toInt() else count
//    for (i in 0..(nodes - 1)) {
//        gen.nextEvents()
//    }
//    gen.end()
//    //        graph.display()
//
//    printGraph(graph, name)
//
//    return graph
//}
//
//fun printGraph(graph: SingleGraph, name: String) {
//    val pic = FileSinkImages(FileSinkImages.OutputType.JPG, Resolutions.UHD_4K)
//    pic.setLayoutPolicy(FileSinkImages.LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);
//    pic.writeAll(graph, "./graph/sample_${name}.png");
//}


//enum class GraphType(val generator: BaseGenerator) {
//
//    RandomGraph(RandomGenerator(3.0)) {
//        override fun createGenerator() = RandomGenerator(3.0)
//    },
//
//    DorogovtsevMendes(DorogovtsevMendesGenerator()) {
//        override fun createGenerator(): BaseGenerator {
//            return DorogovtsevMendesGenerator()
//        }
//    },
//
//    BarabasiAlbert(BarabasiAlbertGenerator(3)) {
//        override fun createGenerator(): BaseGenerator {
//            return BarabasiAlbertGenerator(3)
//        }
//    },
//
//    Grid(GridGenerator()) {
//        override fun createGenerator(): BaseGenerator {
//            return GridGenerator()
//        }
//    },
//
//    RandomEuclidean(RandomEuclideanGenerator()) {
//        override fun createGenerator(): BaseGenerator {
//            return RandomEuclideanGenerator()
//        }
//    };
//
//    abstract fun createGenerator(): BaseGenerator
//}