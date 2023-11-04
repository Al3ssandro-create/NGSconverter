package com.toJson

import NetGraphAlgebraDefs.{NetGraph, NodeObject}
import com.google.common.graph.EndpointPair

import scala.jdk.CollectionConverters.*
import java.io.IOException
import org.json4s.*
import org.json4s.native.Serialization.write
import java.nio.file.{Files, Paths, StandardOpenOption}

object Main {
  private val DefaultInputDir = "./input"
  private val DefaultBaseGraph = "base_graph.ngs"
  private val DefaultPerturbedGraph = "perturbed_graph.ngs.perturbed"
  private val OutputDir = "./output/"

  private def formatDirectoryPath(path: String): String = if (path.endsWith("/")) path else s"$path/"

  private def loadGraph(directory: String, filename: String): NetGraph =
    NetGraph.load(filename, formatDirectoryPath(directory))
      .getOrElse(throw new IOException(s"Unable to load graph from $filename"))

  private def transformEdge(edge: EndpointPair[NodeObject]): CustomEdge =
    CustomEdge(edge.source.id, edge.target.id, edge.target.storedValue)

  private def transformNode(node: NodeObject): CustomNode =
    CustomNode(node.id, node.children, node.props, node.currentDepth, node.propValueRange, node.maxDepth, node.maxBranchingFactor, node.maxProperties, node.storedValue, node.valuableData)

  private def serializeGraph(nodes: List[NodeObject], edges: List[EndpointPair[NodeObject]]): String =
    implicit val formats: Formats = DefaultFormats
    write(CustomGraph(nodes.map(transformNode), edges.map(transformEdge)))

  private def writeToFile(content: String, path: String): Unit =
    Files.write(Paths.get(path), content.getBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)

  private def processAndSaveGraph(directory: String, filename: String, outputFilename: String): Unit =
    val graph = loadGraph(directory, filename)
    val json = serializeGraph(graph.sm.nodes.asScala.toList, graph.sm.edges.asScala.toList)
    writeToFile(json, formatDirectoryPath(OutputDir) + outputFilename)

  def main(args: Array[String]): Unit = {
    val (dir, baseGraph, perturbedGraph) = args match {
      case Array(d, b, p) => (d, b, p)
      case _ =>
        println("Usage: NGStoJson <dir> <base graph> <perturbed graph>")
        (DefaultInputDir, DefaultBaseGraph, DefaultPerturbedGraph)
    }

    processAndSaveGraph(dir, baseGraph, "transformedBaseGraph.json")
    processAndSaveGraph(dir, perturbedGraph, "transformedPerturbedGraph.json")
  }
}
