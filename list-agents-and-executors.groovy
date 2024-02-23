//Get all agent nodes for this controller
def nodes = Jenkins.instance.getNodes()

for (node in nodes) {
  println node.getNodeName() + ' :\n  # of Executors: ' + node.getNumExecutors() + '\n  Root Directory: ' + node.getRemoteFS()
}