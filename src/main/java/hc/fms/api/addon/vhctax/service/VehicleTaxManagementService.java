package hc.fms.api.addon.vhctax.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import hc.fms.api.addon.properties.FmsProperties;
import hc.fms.api.addon.report.util.HttpUtil;
import hc.fms.api.addon.vhctax.entity.ServiceTemplate;
import hc.fms.api.addon.vhctax.entity.VehicleTaxTask;
import hc.fms.api.addon.vhctax.model.VehicleListResponse;
import hc.fms.api.addon.vhctax.repository.ServiceTemplateRepository;
import hc.fms.api.addon.vhctax.repository.TaxPhotoRepository;
import hc.fms.api.addon.vhctax.repository.VehicleTaxPaymentTaskRepository;

@Service
public class VehicleTaxManagementService {
	private RestTemplate restTemplate = new RestTemplate();
	@Autowired
	private ServiceTemplateRepository serviceTemplateRepository;
	@Autowired
	private VehicleTaxPaymentTaskRepository taxPaymentTaskRepository; 
	@Autowired
	private TaxPhotoRepository photoRepository;
	
	@Autowired
	private FmsProperties fmsProps;
	@Autowired
	private HttpHeaders basicUrlEncodedContentTypeHeaders;
	@Autowired
	private ParameterizedTypeReference<VehicleListResponse> vehicleListResponseTypeRef;
	public VehicleListResponse getVehicleList(String hash) {
		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("hash", hash);
		VehicleListResponse response = null;
		try {
			ResponseEntity<VehicleListResponse> responseEntity = restTemplate.exchange(
					String.format("%s%s", fmsProps.getBaseUrl(), fmsProps.getApi().getVehicleList()), 
					HttpMethod.POST, 
					new HttpEntity<>(map, basicUrlEncodedContentTypeHeaders), 
					vehicleListResponseTypeRef
			);
			response= responseEntity.getBody();
		} catch(HttpStatusCodeException  e) {
			try {response = HttpUtil.getObjectMapper().readValue(e.getResponseBodyAsString(), VehicleListResponse.class);} catch(Exception ex) { ex.printStackTrace();}
		}
		return response;
	}
	
	public List<ServiceTemplate> getServiceTemplateList() {
		List<ServiceTemplate> tmplList = serviceTemplateRepository.findAllByOrderById();
		return tmplList;
	}
	public ServiceTemplate createUpdateServiceTemplate(ServiceTemplate svcTmpl) {
		return serviceTemplateRepository.save(svcTmpl);
	}

	public void deleteServiceTemplate(Long id) {
		serviceTemplateRepository.deleteById(id);
	}

	public Optional<ServiceTemplate> getServiceTemplate(Long id) {
		Optional<ServiceTemplate> opt = serviceTemplateRepository.findById(id);
		return opt;
	}
	public VehicleTaxTask createUpdateTaxTask(VehicleTaxTask paymentTaskObj) {
		paymentTaskObj = taxPaymentTaskRepository.save(paymentTaskObj);
		return paymentTaskObj;
	}
	public List<VehicleTaxTask> createUpdateTaxTaskList(List<VehicleTaxTask> taskList) {
		return taxPaymentTaskRepository.saveAll(taskList);
	}

	public List<VehicleTaxTask> listTaxTaskList(String taskType, String fromDate, String toDate) {
		List<String> typeList = null;
		if("".equals(taskType)) {
			typeList = Arrays.asList("T", "C", "K");
		} else {
			typeList = Arrays.asList(taskType);
		}
		System.out.println("in listTaxTaskList " + taxPaymentTaskRepository.listTaxTaskList(typeList, fromDate, toDate));
		
		return taxPaymentTaskRepository.listTaxTaskList(typeList, fromDate, toDate);
	}
	@Transactional
	public void removePaymentTaskListByIdList(List<Long> taskIdList) {
		VehicleTaxTask task;
		for(Long id : taskIdList) {
			task = taxPaymentTaskRepository.findById(id).get();
			if(task.getPhotoId()!= null) {
				photoRepository.deleteById(task.getPhotoId());
			}
			if(task.getReceiptPhotoId()!= null) {
				photoRepository.deleteById(task.getReceiptPhotoId());
			}
			taxPaymentTaskRepository.deleteById(id);
		}
	}
	@Transactional
	public void makePaymentTaskListPaid(List<Long> taskIdList) {
		for(Long id : taskIdList) taxPaymentTaskRepository.updatePaymentTaskPaid(id);
	}
	@Transactional
	public void downloadPDFfile() {
		System.out.print("Hello");
	}
	
	
}
