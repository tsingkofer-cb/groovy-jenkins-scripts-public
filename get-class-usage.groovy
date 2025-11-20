String className = "org.bouncycastle.asn1.ASN1ObjectIdentifier"

def clazzResource = className.replaceAll("\\.", "/") + ".class"
println "Looking for: ${clazzResource}\n"
Jenkins.instance.pluginManager.activePlugins.forEach { 
  PluginWrapper plugin ->def c = plugin.classLoader.getResources(clazzResource)
  if (c.hasMoreElements()) 
  {println "Found in ${plugin}"
   println Collections.list(c).join("\n") + "\n"}}

return
