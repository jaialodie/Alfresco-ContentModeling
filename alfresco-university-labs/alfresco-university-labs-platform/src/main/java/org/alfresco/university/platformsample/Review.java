package org.alfresco.university.platformsample;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;


import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Review
        implements NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnUpdateNodePolicy {

    // Dependencies
    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private ContentService contentService;
    private ServiceRegistry serviceRegistry;

    // Behaviours
    private Behaviour onCreateNode;

    private Logger logger = LoggerFactory.getLogger(Review.class);


    public void init() {
        if (logger.isDebugEnabled()) logger.debug("Initializing review behaviors");

        // Create behaviours
        this.onCreateNode = new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT);


        // Bind behaviours to node policies
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"), QName.createQName(ReviewModel.NAMESPACE_ALFRESCO_REVIEW_CONTENT_MODEL, ReviewModel.ASPECT_SCR_REVIEWABLE), this.onCreateNode);
    }

    public void onCreateNode(ChildAssociationRef parentChildAssocRef) {
        if (logger.isDebugEnabled()) logger.debug("Inside onCreateNode");
        //
        NodeRef parentFolderRef = parentChildAssocRef.getParentRef();
        NodeRef docRef = parentChildAssocRef.getChildRef();

        // Check if node exists, might be moved, or created and deleted in same transaction.
        if (docRef == null || !serviceRegistry.getNodeService().exists(docRef)) {
            // Does not exist, nothing to do
            logger.warn("onAddDocument: A new document was added but removed in same transaction");
            return;
        } else {
            logger.info("onAddDocument: A new document with ref ({}) was just created in folder ({})",
                    docRef, parentFolderRef);
            //
            ContentReader reader = contentService.getReader(docRef, ContentModel.PROP_CONTENT);
            int numOfWords = Contador(reader);

            nodeService.setProperty(
                    docRef,
                    QName.createQName(
                            ReviewModel.NAMESPACE_ALFRESCO_REVIEW_CONTENT_MODEL,
                            ReviewModel.PROP_NUM_OF_WORDS),
                    numOfWords);

        }
    }

    @Override
    public void onUpdateNode(NodeRef docRef) {
        if (logger.isDebugEnabled()) logger.debug("Inside onCreateNode");

        // Check if node exists, might be moved, or created and deleted in same transaction.
        if (docRef == null || !serviceRegistry.getNodeService().exists(docRef)) {
            // Does not exist, nothing to do
            logger.warn("onUpdateDocument: A document was updated but removed in same transaction");
            return;
        } else {
            logger.info("onUpdateDocument: A new document with ref ({}) was just updated",
                    docRef);
            //
            ContentReader reader = contentService.getReader(docRef, ContentModel.PROP_CONTENT);
            int numOfWords = Contador(reader);

            nodeService.setProperty(
                    docRef,
                    QName.createQName(
                            ReviewModel.NAMESPACE_ALFRESCO_REVIEW_CONTENT_MODEL,
                            ReviewModel.PROP_NUM_OF_WORDS),
                    numOfWords);
        }
    }

    public Integer Contador (ContentReader reader) {
        int contador = 0;
		try {
            BufferedReader br = new BufferedReader((Reader) reader);
            System.out.println("TEXTO LEIDO");
            System.out.println("----- -----\n");

            String linea = br.readLine();
            while (linea != null) {
                //Imprimir cada liena en consola
                    //System.out.println(linea);
                //Troceamos línea cortando donde haya espacios en blanco
                String[] palabras = linea.split(" ");
                //Acumulamos las palabras(trozos) obtenidos
                contador += palabras.length;
                //Leemos siguiente línea
                linea = br.readLine();
            }
            br.close();
            return contador;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }




    public NodeService getNodeService() {
        return nodeService;
    }


    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }


    public PolicyComponent getPolicyComponent() {
        return policyComponent;
    }


    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }


}