import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider
import com.cloudbees.hudson.plugins.folder.Folder

def instance = Jenkins.get()

// Update credentials in all domains from the system credentials provider
def systemProvider = instance.getExtensionList(SystemCredentialsProvider.class)
if (!systemProvider.empty) {
    def systemStore = systemProvider.first().getStore()
    println 'Updating credentials in system level credential store...'
    for (domain in systemStore.domains) {
        for (credential in systemStore.getCredentials(domain)) {
            if (credential.isUsernameSecret()){
                println "Updating credential: ${credential.id} for username ${credential.username}"
                credential.setUsernameSecret(false)
                systemStore.updateCredentials(domain, credential, credential)
            } else {
                println "Username ${credential.username} for credential ID ${credential.id} was already not secret."
            }
        }
    }
}

// Update credentials in all domains from all folders
def folderExtension = instance.getExtensionList(FolderCredentialsProvider.class)
if (!folderExtension.empty) {
    def folders = instance.getAllItems(Folder.class)
    def folderProvider = folderExtension.first()
    for (folder in folders) {
        println ''
        println 'Updating credentials in folder: ' + folder.name
        def store = folderProvider.getStore(folder)
        for (domain in store.domains) {
            for (credential in store.getCredentials(domain)) {
                if (credential.isUsernameSecret()){
                    println "Updating credential: ${credential.id} for username ${credential.username}"
                    credential.setUsernameSecret(false)
                    store.updateCredentials(domain, credential, credential)
                } else {
                    println "Username ${credential.username} for credential ID ${credential.id} was already not secret."
                }
            }
        }
    }
}