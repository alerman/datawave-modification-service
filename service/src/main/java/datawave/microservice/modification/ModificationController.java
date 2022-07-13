package datawave.microservice.modification;

import com.codahale.metrics.annotation.Timed;
import datawave.microservice.authorization.user.ProxiedUserDetails;
import datawave.modification.ModificationService;
import datawave.webservice.modification.ModificationRequestBase;
import datawave.webservice.result.VoidResponse;
import datawave.webservice.results.modification.ModificationConfigurationResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static datawave.microservice.http.converter.protostuff.ProtostuffHttpMessageConverter.PROTOSTUFF_VALUE;

@RestController
@RequestMapping(path = "/modification/v1", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE,
        PROTOSTUFF_VALUE, MediaType.TEXT_HTML_VALUE, "text/x-yaml", "application/x-yaml"})
public class ModificationController {
    
    private final ModificationService service;
    
    public ModificationController(ModificationService service) {
        this.service = service;
    }
    
    /**
     * Returns a list of the Modification service names and their configurations
     *
     * @return datawave.webservice.results.modification.ModificationConfigurationResponse
     * @RequestHeader X-ProxiedEntitiesChain use when proxying request for user
     * @RequestHeader X-ProxiedIssuersChain required when using X-ProxiedEntitiesChain, specify one issuer DN per subject DN listed in X-ProxiedEntitiesChain
     * @ResponseHeader X-OperationTimeInMS time spent on the server performing the operation, does not account for network or result serialization
     */
    @GetMapping("/listConfigurations")
    @Timed(name = "dw.modification.list.configurations", absolute = true)
    public List<ModificationConfigurationResponse> listConfigurations() {
        return service.listConfigurations();
    }
    
    /**
     * Execute a Modification service with the given name and runtime parameters
     *
     * @param modificationServiceName
     *            Name of the modification service configuration
     * @param request
     *            object type specified in listConfigurations response.
     * @return datawave.webservice.result.VoidResponse
     * @RequestHeader X-ProxiedEntitiesChain use when proxying request for user
     * @RequestHeader X-ProxiedIssuersChain required when using X-ProxiedEntitiesChain, specify one issuer DN per subject DN listed in X-ProxiedEntitiesChain
     * @ResponseHeader X-OperationTimeInMS time spent on the server performing the operation, does not account for network or result serialization
     * @HTTP 200 success
     * @HTTP 400 if jobName is invalid
     * @HTTP 401 if user does not have correct roles
     * @HTTP 500 error starting the job
     */
    @PostMapping(path = "/{serviceName}/submit", consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @Timed(name = "dw.modification.data.submit", absolute = true)
    public VoidResponse submit(@PathVariable String modificationServiceName, @RequestParam("request") ModificationRequestBase request,
                    @AuthenticationPrincipal ProxiedUserDetails currentUser) {
        return service.submit(currentUser.getProxiedUsers(), modificationServiceName, request);
    }
    
}
