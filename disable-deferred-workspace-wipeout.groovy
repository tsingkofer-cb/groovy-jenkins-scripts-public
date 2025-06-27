import hudson.plugins.ws_cleanup.DisableDeferredWipeoutNodeProperty

nodes = Jenkins.getInstance().getNodes()

for (node in nodes) {
	println node.name
    if (node.getNodeProperty(DisableDeferredWipeoutNodeProperty)){
        println '  Deferred Wipeout already disabled for this node.'
    } else {
        println '  Disabling Deferred Wipeout now...'
        node.getNodeProperties().add(new DisableDeferredWipeoutNodeProperty())
    }
}
