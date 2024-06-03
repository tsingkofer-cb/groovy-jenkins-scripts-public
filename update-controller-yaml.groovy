// Iterates through all managed controllers in a CJOC and updates the defined yaml for each. 
// Will require a reprovision of the controllers in order to take effect.

def newYaml = '''---
kind: StatefulSet
apiVersion: apps/v1
spec:
  template:
    spec:
      tolerations:
      - key: samplekey1
        operator: Exists
        effect: NoSchedule
      - key: samplekey2
        operator: Exists
        effect: NoExecute
'''
def mms = Jenkins.instance.getAllItems(com.cloudbees.opscenter.server.model.ManagedMaster)

mms.each {
  mmConfig = it.getConfiguration()
  println it.name
  println '**************CURRENT YAML**************'
  println mmConfig.getYaml()
  println '****************************'
  println 'Updating controller yaml for ' + it.name + '...' 
  mmConfig.setYaml(newYaml)
  it.setConfiguration(mmConfig)
	it.save()
}
null
