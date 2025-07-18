// Iterates through all managed controllers in a CJOC and updates the cpu and memory for each. 
// Will require a reprovision of the controllers in order to take effect.

def mms = Jenkins.instance.getAllItems(com.cloudbees.opscenter.server.model.ManagedMaster)

mms.each {
    mmConfig = it.getConfiguration()
    println it.name
    println '**************CURRENT CONFIG**************'
    println mmConfig.cpus + ' cpus'
    println mmConfig.memory + ' MB memory'
    println '******************************************'
    println 'Updating controller configs for ' + it.name + '...' 
    mmConfig.setCpus(3)
    mmConfig.setMemory(4096)
    it.setConfiguration(mmConfig)
    it.save()
}
null
