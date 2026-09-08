package com.youngone.mediacontrol.identity.application;

import com.youngone.mediacontrol.identity.api.HrModels;
import com.youngone.mediacontrol.identity.domain.HrIntegrationSource;
import com.youngone.mediacontrol.identity.infra.HrIntegrationSourceRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;
import java.time.Instant;import java.util.*;

@Service public class HrSourceService{
 private final HrIntegrationSourceRepository sources;private final ObjectMapper json;
 public HrSourceService(HrIntegrationSourceRepository sources,ObjectMapper json){this.sources=sources;this.json=json;}
 @Transactional public UUID create(HrModels.SourceCreate r,String actor){String config=r.configJson().toLowerCase(Locale.ROOT);try{if(!json.readTree(r.configJson()).isObject())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"configJson must be a JSON object");}catch(ResponseStatusException e){throw e;}catch(Exception e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"configJson is invalid JSON");}if(config.contains("password")||config.contains("token")||config.contains("clientsecret")||config.contains("privatekey"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Credentials must use secretRef, not configJson");return sources.save(new HrIntegrationSource(UUID.randomUUID(),r.name(),r.connectorType(),r.syncMode(),r.authorityRank(),r.configJson(),r.secretRef(),actor,Instant.now())).getId();}
}