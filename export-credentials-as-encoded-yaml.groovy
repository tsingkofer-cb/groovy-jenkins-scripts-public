// This outputs a yaml file that can be used with kubectl apply to create in cluster
// Values are base64 encoded (since kubectl requires that)


import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.domains.Domain
import jenkins.model.Jenkins

def indent = { String text, int indentationCount ->
    def replacement = " " * indentationCount
    text.replaceAll("(?m)^", replacement)
}

println "apiVersion: v1"
println "kind: Secret"
println "metadata:"
println "  name: oc-secrets"
println "data:"

Jenkins.get().allItems().collectMany{ CredentialsProvider.lookupStores(it).toList()}.unique().forEach { store ->
    Map<Domain, List<Credentials>> domainCreds = [:]
    store.domains.each { domainCreds.put(it, store.getCredentials(it))}
    if (domainCreds.collectMany{ it.value}.empty) {
        return
    }
    domainCreds.forEach { domain , creds ->
        creds.each { cred ->
          cred.properties.each { prop, val ->
              def encodedVal ='' 
              if (!prop.equals("content")) {
                encodedVal = val.toString().bytes.encodeBase64().toString()
              } else {
                encodedVal = val.text.toString().bytes.encodeBase64().toString()
              }
              if (prop.equals("password")){  
                println indent(cred.id + "Password: " + encodedVal, 2)
              } else if (prop.equals("privateKey")){
                println indent(cred.id + "PrivateKey: |\n" + indent(encodedVal, 2), 2)
              } else if (prop.equals("username")){
                println indent(cred.id + "Username: " + encodedVal, 2)
              } else if (prop.equals("secret")){
                println indent(cred.id + "Secret: " + encodedVal, 2)
              } else if (prop.equals("content")){
                println indent(cred.id + "SecretFile: |\n" + indent(encodedVal, 2), 2)
              } else if (prop.equals("secretKey")){
                println indent(cred.id + "SecretKey: " + encodedVal, 2)
              } else if (prop.equals("accessKey")){
                println indent(cred.id + "AccessKey: " + encodedVal, 2)
              } 
            }
        }
    }
}
