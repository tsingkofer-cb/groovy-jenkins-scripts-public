// Based on https://github.com/cloudbees/jenkins-scripts/blob/master/credentials-migration/export-credentials-folder-level.groovy

// Use this script in case you need to export (recursively) the credentials from a single folder on a controller. (The original script exports from all folders)
// Make sure to update the full folder name that you want to start the recursive export from on line 28.
// ex. 'top-level' or 'top-level/subfolder'

// You can then use the original import/update script to import the output of this script into the target controller (the identical folder structure must exist there): 
// https://github.com/cloudbees/jenkins-scripts/blob/master/credentials-migration/update-credentials-folder-level.groovy

import com.cloudbees.hudson.plugins.folder.AbstractFolder
import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider
import com.cloudbees.plugins.credentials.domains.DomainCredentials
import com.thoughtworks.xstream.converters.Converter
import com.thoughtworks.xstream.converters.MarshallingContext
import com.thoughtworks.xstream.converters.UnmarshallingContext
import com.thoughtworks.xstream.io.HierarchicalStreamReader
import com.thoughtworks.xstream.io.HierarchicalStreamWriter
import com.trilead.ssh2.crypto.Base64
import hudson.util.Secret
import hudson.util.XStream2
import jenkins.model.Jenkins

def instance = Jenkins.get()
def credentials = []
HashMap<String, List<DomainCredentials>> domainsFromFolders = new HashMap<String, List<DomainCredentials>>();

//Parent folder name to start with
String folderName = 'top-level/subfolder'
AbstractFolder parentFolder = Jenkins.instance.getAllItems(AbstractFolder.class).find{ (it.fullName == folderName) }
print "Folder : " + parentFolder.name + "\n"

// Each folder contains a Store. A Store contains one or more Domains 
// and each Domain might contain Credentials defined. 
def folderExtension = instance.getExtensionList(FolderCredentialsProvider.class)
if (!folderExtension.empty) {
    def folders = parentFolder.getAllItems(AbstractFolder.class)
  	folders.add(0, parentFolder)
    def folderProvider = folderExtension.first()
    def domainName
    def store
    def listDomainCredentials
    for (folder in folders) {
        store = folderProvider.getStore(folder)
        println "Processing Store for  " + store.getContext().getUrl()
        listDomainCredentials = new ArrayList<DomainCredentials>();
        for (domain in store.domains) {
            domainName = domain.isGlobal() ? "Global":domain.getName();
            println "   Processing Domain " + domainName
            listDomainCredentials.add(new DomainCredentials(domain, store.getCredentials(domain)));
        }
        println "       Adding all credentials in the Store... "
        domainsFromFolders.put(store.getContext().getUrl(), listDomainCredentials);
    }
}

// The converter ensures that the output XML contains the unencrypted secrets
def converter = new Converter() {
    @Override
    void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {
        writer.value = Secret.toString(object as Secret)
    }

    @Override
    Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) { null }

    @Override
    boolean canConvert(Class type) { type == Secret.class }
}

def stream = new XStream2()
stream.registerConverter(converter)

// Marshal the list of credentials into XML
def encoded = []

    def xml = Base64.encode(stream.toXML(domainsFromFolders).bytes)
    encoded.add("\"${xml}\"")

println encoded.toString()
