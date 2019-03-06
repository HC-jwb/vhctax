package hc.fms.api.addon.report.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import hc.fms.api.addon.properties.FmsProperties;
import hc.fms.api.addon.report.entity.FillDrainStatistics;
import hc.fms.api.addon.report.entity.FuelStatResult;
import hc.fms.api.addon.report.entity.GenSection;
import hc.fms.api.addon.report.entity.ReportGen;
import hc.fms.api.addon.report.model.ExportableReport;
import hc.fms.api.addon.report.model.GroupResponse;
import hc.fms.api.addon.report.model.ReportGenFlatRequest;
import hc.fms.api.addon.report.model.ReportGenResponse;
import hc.fms.api.addon.report.model.ReportResponse;
import hc.fms.api.addon.report.model.SensorResponse;
import hc.fms.api.addon.report.model.TrackerResponse;
import hc.fms.api.addon.report.model.TripResponse;
import hc.fms.api.addon.report.model.fuel.FuelEffRateStatSection;
import hc.fms.api.addon.report.model.fuel.FuelMileageSection;
import hc.fms.api.addon.report.model.fuel.FuelStat;
import hc.fms.api.addon.report.model.fuel.MeasurementSensorPlugin;
import hc.fms.api.addon.report.model.fuel.ReportDesc;
import hc.fms.api.addon.report.model.fuel.filldrain.FillDrainSection;
import hc.fms.api.addon.report.model.fuel.filldrain.FillDrainStatSection;
import hc.fms.api.addon.report.model.fuel.filldrain.ObdFuelPlugin;
import hc.fms.api.addon.report.model.tracker.request.GenerateRequest;
import hc.fms.api.addon.report.model.tracker.request.SensorRequest;
import hc.fms.api.addon.report.model.tracker.request.TrackerInfo;
import hc.fms.api.addon.report.model.tracker.request.TripRequest;
import hc.fms.api.addon.report.repository.FillDrainStatisticsRepository;
import hc.fms.api.addon.report.repository.FuelStatisticsRepository;
import hc.fms.api.addon.report.repository.GenSectionRepository;
import hc.fms.api.addon.report.repository.ReportGenRepository;
import hc.fms.api.addon.report.util.HttpUtil;
import hc.fms.api.addon.vhctax.entity.VehicleTaxTask;

@Service
public class TrackerService {
	private RestTemplate restTemplate = new RestTemplate();
	private Logger logger = LoggerFactory.getLogger(TrackerService.class);
	@Autowired
	private ReportProcessor reportProcessor;
	@Autowired
	private HttpHeaders basicUrlEncodedContentTypeHeaders;
	@Autowired
	private FmsProperties fmsProps;
	@Autowired
	private ParameterizedTypeReference<TripResponse> tripResponseTypeRef;
	@Autowired
	private ParameterizedTypeReference<SensorResponse> sensorResponseTypeRef;
	@Autowired
	private ParameterizedTypeReference<GroupResponse> groupResponseTypeRef;
	@Autowired
	private ParameterizedTypeReference<ReportGenResponse> reportGenResponseTypeRef;
	@Autowired
	private ParameterizedTypeReference<ReportResponse<FuelMileageSection>> reportConsumptionResponseTypeRef;
	@Autowired
	private ParameterizedTypeReference<ReportResponse<FillDrainSection>> reportFillDrainResponseTypeRef;
	@Autowired
	private ParameterizedTypeReference<TrackerResponse> trackerResponseTypeRef;
	
	@Autowired
	private FuelStatisticsRepository fuelStatRepository;
	@Autowired
	private ReportGenRepository reportGenRepository;
	@Autowired
	private GenSectionRepository sectionRepository;
	@Autowired
	private FillDrainStatisticsRepository fillDrainStatRepository;
	public TrackerResponse getTrackerList(String hash) {
		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("hash", hash);
		TrackerResponse response = null;
		try {
			ResponseEntity<TrackerResponse> responseEntity = restTemplate.exchange(
					String.format("%s%s", fmsProps.getBaseUrl(), fmsProps.getApi().getTracker()), 
					HttpMethod.POST, 
					new HttpEntity<>(map, basicUrlEncodedContentTypeHeaders), 
					trackerResponseTypeRef
			);
			response= responseEntity.getBody();
		} catch(HttpStatusCodeException  e) {
			try {response = HttpUtil.getObjectMapper().readValue(e.getResponseBodyAsString(), TrackerResponse.class);} catch(Exception ex) { ex.printStackTrace();}
		}
		return response;
	}
	public TripResponse getTrip(TripRequest req) {
		return getTrip(req.getHash(), req.getTrackerId(), req.getFrom(), req.getTo());
	}
	public TripResponse getTrip(String hash, int trackerId, String from, String to) {
		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("tracker_id", String.valueOf(trackerId));
		map.add("from", from);
		map.add("to", to);
		map.add("hash", hash);
		TripResponse response = null;
		try {
			ResponseEntity<TripResponse> responseEntity = restTemplate.exchange(String.format("%s%s", fmsProps.getBaseUrl(), fmsProps.getApi().getTrip()), HttpMethod.POST, new HttpEntity<>(map, basicUrlEncodedContentTypeHeaders), tripResponseTypeRef);
			response= responseEntity.getBody();
		} catch(HttpStatusCodeException  e) {
			try {response = HttpUtil.getObjectMapper().readValue(e.getResponseBodyAsString(), TripResponse.class);} catch(Exception ex) {	ex.printStackTrace();}
		}
		return response;
	}
	public SensorResponse getSensorList(SensorRequest req) {
		return getSensorList(req.getHash(), req.getTrackerId());
	}
	public SensorResponse getSensorList(String hash, int trackerId) {
		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("tracker_id", String.valueOf(trackerId));
		map.add("hash", hash);
		SensorResponse response = null;
		try {
			ResponseEntity<SensorResponse> responseEntity = restTemplate.exchange(String.format("%s%s", fmsProps.getBaseUrl(), fmsProps.getApi().getTrackerSensor()), HttpMethod.POST, new HttpEntity<>(map, basicUrlEncodedContentTypeHeaders), sensorResponseTypeRef);
			response= responseEntity.getBody();
		} catch(HttpStatusCodeException  e) {
			try {response = HttpUtil.getObjectMapper().readValue(e.getResponseBodyAsString(), SensorResponse.class);} catch(Exception ex) {	ex.printStackTrace();}
		}
		return response;
	}
	public GroupResponse getGroupList(String hash) {
		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("hash", hash);
		GroupResponse response = null;
		try {
			ResponseEntity<GroupResponse> responseEntity = restTemplate.exchange(String.format("%s%s", fmsProps.getBaseUrl(), fmsProps.getApi().getTrackerGroup()), HttpMethod.POST, new HttpEntity<>(map, basicUrlEncodedContentTypeHeaders), groupResponseTypeRef);
			response= responseEntity.getBody();
		} catch(HttpStatusCodeException  e) {
			try {response = HttpUtil.getObjectMapper().readValue(e.getResponseBodyAsString(), GroupResponse.class);} catch(Exception ex) {	ex.printStackTrace();}
		}
		return response;
	}
	public <T>ReportGenResponse requestReportGen(GenerateRequest<T> req) {
		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("hash", req.getHash());
		map.add("from", req.getFrom());
		map.add("to", req.getTo());
		map.add("geocoder", req.getGeocoder());
		
		map.add("trackers", req.getTrackers());
		map.add("plugin", req.getPlugin());
		map.add("time_filter", req.getTimeFilter());
		ReportGenResponse response = null;
		try {
			ResponseEntity<ReportGenResponse> responseEntity = restTemplate.exchange(String.format("%s%s", fmsProps.getBaseUrl(), fmsProps.getApi().getReportGen()), HttpMethod.POST, new HttpEntity<>(map, basicUrlEncodedContentTypeHeaders), reportGenResponseTypeRef);
			response= responseEntity.getBody();
		} catch(HttpStatusCodeException  e) {
			try {response = HttpUtil.getObjectMapper().readValue(e.getResponseBodyAsString(), ReportGenResponse.class);} catch(Exception ex) {	ex.printStackTrace();}
		}
		return response;
	}
	@SuppressWarnings("unchecked")
	public ReportResponse<FillDrainSection> retrieveFillDrainReport(String hash, long reportId) {
		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("hash", hash);
		map.add("report_id", String.valueOf(reportId));
		ReportResponse<FillDrainSection> response = null;
		try {
			ResponseEntity<ReportResponse<FillDrainSection>> responseEntity = restTemplate.exchange(String.format("%s%s", fmsProps.getBaseUrl(), fmsProps.getApi().getReportRetrieve()), HttpMethod.POST, new HttpEntity<>(map, basicUrlEncodedContentTypeHeaders), reportFillDrainResponseTypeRef);
			response= responseEntity.getBody();
		} catch(HttpStatusCodeException e) {
			try {response = HttpUtil.getObjectMapper().readValue(e.getResponseBodyAsString(), ReportResponse.class);} catch(Exception ex) {ex.printStackTrace();}
		}
		return response;
	}
	@SuppressWarnings("unchecked")
	public ReportResponse<FuelMileageSection> retrieveFuelMileageReport(String hash, long reportId) {
		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("hash", hash);
		map.add("report_id", String.valueOf(reportId));
		ReportResponse<FuelMileageSection> response = null;
		try {
			ResponseEntity<ReportResponse<FuelMileageSection>> responseEntity = restTemplate.exchange(String.format("%s%s", fmsProps.getBaseUrl(), fmsProps.getApi().getReportRetrieve()), HttpMethod.POST, new HttpEntity<>(map, basicUrlEncodedContentTypeHeaders), reportConsumptionResponseTypeRef);
			response= responseEntity.getBody();
		} catch(HttpStatusCodeException e) {
			try {response = HttpUtil.getObjectMapper().readValue(e.getResponseBodyAsString(), ReportResponse.class);} catch(Exception ex) {ex.printStackTrace();}
		}
		return response;
	}
	
	public ReportGenResponse generateFuelMileageReport(ReportGenFlatRequest req) {
		List<TrackerInfo> infoList = req.getTrackers();
		List<Integer> trackerIdList = infoList.stream().map(info -> info.getTrackerId()).collect(Collectors.toList());
		
		String from = req.getFrom();
		String to = req.getTo();
		String hash = req.getHash();
		int detailsIntervalMinutes = req.getIntervalInMin();
		
		GenerateRequest<MeasurementSensorPlugin> fuelGenReq = new GenerateRequest<>();
		fuelGenReq.setHash(hash);

		fuelGenReq.setTrackers(trackerIdList);
		fuelGenReq.setFrom(from);
		fuelGenReq.setTo(to);
		
		MeasurementSensorPlugin plugin = new MeasurementSensorPlugin();
		plugin.setDetailsIntervalMinutes(detailsIntervalMinutes);
		//logger.info(infoList.stream().map(info -> new Plugin.Sensor(info.getTrackerId(), info.getFuelConsumptionSensorId())).collect(Collectors.toList()) + "");
		plugin.setSensors(infoList.stream().map(info -> new MeasurementSensorPlugin.Sensor(info.getTrackerId(), info.getFuelConsumptionSensorId())).collect(Collectors.toList()));
		fuelGenReq.setPlugin(plugin);
				
		
		GenerateRequest<MeasurementSensorPlugin> mileageGenReq = new GenerateRequest<>();
		mileageGenReq.setHash(hash);

		mileageGenReq.setTrackers(trackerIdList);
		mileageGenReq.setFrom(from);
		mileageGenReq.setTo(to);
		
		plugin = new MeasurementSensorPlugin();
		plugin.setDetailsIntervalMinutes(detailsIntervalMinutes);
		plugin.setSensors(infoList.stream().map(info -> new MeasurementSensorPlugin.Sensor(info.getTrackerId(), info.getHardwareMileageSensorId())).collect(Collectors.toList()));
		mileageGenReq.setPlugin(plugin);
		
		ReportGenResponse response = new ReportGenResponse();
		ReportGenResponse fuelGenResponse = requestReportGen(fuelGenReq);
		if(fuelGenResponse.isSuccess()) {
			ReportGenResponse mileageGenResponse = requestReportGen(mileageGenReq);
			if(mileageGenResponse.isSuccess()) {
				ReportGen reportGen = new ReportGen();
				reportGen.setFuelReportId(fuelGenResponse.getId());
				reportGen.setMileageReportId(mileageGenResponse.getId());
				reportGen.setLabel(req.getLabel());
				reportGen.setFrom(from);
				reportGen.setTo(to);
				reportGen.setClientId(req.getClientId());
				final ReportGen reportGenSaved = reportProcessor.logReportGen(reportGen);
				response.setSuccess(true);
				response.setId(reportGenSaved.getId());
				ExecutorService execService = Executors.newSingleThreadExecutor();
				
				execService.submit(() -> {
					long elapsed = 0L, start = System.currentTimeMillis();
					long fuelReportId = reportGenSaved.getFuelReportId();
					long mileageReportId = reportGenSaved.getMileageReportId();
					ReportDesc<FuelMileageSection> fuelReport = null, mileageReport = null;
					while(true) {
						try {
							Thread.sleep(5 * 1000);
						} catch(Exception e) {
							e.printStackTrace();
						}
						if(fuelReport == null) {
							try {
								ReportResponse<FuelMileageSection> fuelReportResponse = retrieveFuelMileageReport(req.getHash(), fuelReportId);
								if(fuelReportResponse.isSuccess()) {
									fuelReport = fuelReportResponse.getReport();
								} else if(fuelReportResponse.getStatus().getCode() == 229) {
									logger.info("Fuel consumption report generation still in progress ..");
								} else if(fuelReportResponse.getStatus().getCode() == 204) {
									logger.info("Fuel consumption report not found");
									break;
								} else {
									logger.info("Fuel consumption report unknown error status " + fuelReportResponse);
								}
							} catch(Exception e) {
								e.printStackTrace();
							}
						}
						if(mileageReport == null) {
							try {
								ReportResponse<FuelMileageSection> mileageReportResponse = retrieveFuelMileageReport(req.getHash(), mileageReportId);
								if(mileageReportResponse.isSuccess()) {
									mileageReport = mileageReportResponse.getReport();
								} else if(mileageReportResponse.getStatus().getCode() == 229) {
									logger.info("Mileage report generation still in progress ..");
								} else if(mileageReportResponse.getStatus().getCode() == 204) {
									logger.info("Mileage report not found");
									break;
								} else {
									logger.info("Mileage report unknown error status " + mileageReportResponse);
								}
							} catch(Exception e) {
								//e.printStackTrace();
							}
						}
						if(fuelReport != null && mileageReport != null) {
							logger.info("retrieved both reports " + reportGenSaved);
							try {
								reportProcessor.processFuelMileageReport(reportGenSaved, fuelReport, mileageReport);
							} catch(Exception e) {
								e.printStackTrace();
							}
							break;
						}
						elapsed = System.currentTimeMillis() - start;
						if(elapsed > 60 * 1000 * 60) {
							logger.info("Waited too long for fuel report to be ready... try exiting thread");
							break;
						}
					}
					logger.info("Report generation Thread exited");
				});
				execService.shutdown();
			} else {
				return mileageGenResponse;//fuel generation successful but failed to generate mileageReport 
			}
		} else {
			return fuelGenResponse;//fuel generation failed
		}
		return response;//all successful and created a single report reponse which includes both of the sub reports
	}
	public ReportGenResponse generateFillDrainReport(ReportGenFlatRequest req) {
		ReportGenResponse response = new ReportGenResponse();
		List<TrackerInfo> infoList = req.getTrackers();
		List<Integer> trackerIdList = infoList.stream().map(info -> info.getTrackerId()).collect(Collectors.toList());
		
		String from = req.getFrom();
		String to = req.getTo();
		String hash = req.getHash();
		
		GenerateRequest<ObdFuelPlugin> fillDrainGenReq = new GenerateRequest<>();
		fillDrainGenReq.setHash(hash);
		fillDrainGenReq.setTrackers(trackerIdList);
		fillDrainGenReq.setFrom(from);
		fillDrainGenReq.setTo(to);
		
		ObdFuelPlugin plugin = new ObdFuelPlugin();
		fillDrainGenReq.setPlugin(plugin);
		ReportGenResponse fillDrainGenResponse = requestReportGen(fillDrainGenReq);
		
		if(fillDrainGenResponse.isSuccess()) {
			ReportGen reportGen = new ReportGen();
			reportGen.setFillDrainReportId(fillDrainGenResponse.getId());
			reportGen.setLabel(req.getLabel());
			reportGen.setFrom(from);
			reportGen.setTo(to);
			reportGen.setClientId(req.getClientId());
			final ReportGen reportGenSaved = reportProcessor.logReportGen(reportGen);
			response.setSuccess(true);
			response.setId(reportGenSaved.getId());
			ExecutorService execService = Executors.newSingleThreadExecutor();
			execService.submit(() -> {
				long elapsed = 0L, start = System.currentTimeMillis();
				long fillDrainReportId = reportGenSaved.getFillDrainReportId();
				ReportDesc<FillDrainSection> fillDrainReport = null;
				while(true) {
					try {
						Thread.sleep(5 * 1000);
					} catch(Exception e) {
						e.printStackTrace();
					}
					if(fillDrainReport == null) {
						try {
							ReportResponse<FillDrainSection> fillDrainReportResposne = retrieveFillDrainReport(req.getHash(), fillDrainReportId);
							if(fillDrainReportResposne.isSuccess()) {
								fillDrainReport = fillDrainReportResposne.getReport();
								logger.info("retrieved FillDrain reports " + reportGenSaved);
								try {
									reportProcessor.processFillDrainReport(reportGenSaved, fillDrainReport);
								} catch(Exception e) {
									e.printStackTrace();
								}
								break;
							} else if(fillDrainReportResposne.getStatus().getCode() == 229) {
								logger.info("FillDrain report generation still in progress ..");
							} else if(fillDrainReportResposne.getStatus().getCode() == 204) {
								logger.info("FillDrain report not found");
								break;
							} else {
								logger.info("FillDrain report unknown error status " + fillDrainReportResposne);
							}
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
					elapsed = System.currentTimeMillis() - start;
					if(elapsed > 60 * 1000 * 60) {
						logger.info("Waited too long for fill drain report to be ready... try exiting thread");
						break;
					}
				}
				logger.info("FillDrain Report generation Thread exited");
			});
			execService.shutdown();
		} else {
			return fillDrainGenResponse;
		}
		return response;
	}
	public List<FuelStatResult> getFuelStatisticsResultListByReportIdAndTrackerId(Long reportId, Long trackerId) {
		return fuelStatRepository.getFuelStatResultList(reportId, trackerId);
	}
	public List<FillDrainStatistics> getFillDrainStatisticsByReportIdAndTrackerId(Long reportId, Long trackerId) {
		return fillDrainStatRepository.findAllByReportIdAndTrackerIdOrderByEventIdAsc(reportId, trackerId);
	}
	public List<ReportGen> getFuelReportGenList(String clientId) {
		return reportGenRepository.findAllFuelEffReportByClientIdOrderByCreatedDateDesc(clientId);
	}
	public List<ReportGen> getFillDrainReportGenList(String clientId) {
		return reportGenRepository.findAllFillDrainReportByClientIdOrderByCreatedDateDesc(clientId);
	}
	public List<Long> getReportGenListInProgress(String clientId) {
		List<ReportGen> genList = reportGenRepository.findAllInProgressFuelEffRateGenByClientId(clientId);
		return genList.stream().map(reportGen -> reportGen.getId()).collect(Collectors.toList());
	}
	public List<Long> getFillDrainReportGenListInProgress(String clientId) {
		List<ReportGen> genList = reportGenRepository.findAllInProgressFillDrainGenByClientId(clientId);
		return genList.stream().map(reportGen -> reportGen.getId()).collect(Collectors.toList());
	}
	public ReportGen getReportGen(Long genId) {
		return reportGenRepository.findById(genId).get();
	}
	public List<GenSection> getSectionListFor(Long reportId) {
		return sectionRepository.findAllByReportId(reportId);
	}
	public ExportableReport<?> getExportableReport(Long reportId) {
		ReportGen reportGen = getReportGen(reportId);
		if(reportGen.getFillDrainReportId() != null) {
			return getExportableFillDrainReport(reportGen);
		} else {
			return getFuelEffRateExportableReport(reportGen);
		}
	}
/*
	public ExportableReport<?> getExportableTaxReport(List<VehicleTaxTask> listTax) {
		
		ExportableReport<FuelEffRateStatSection> report = new ExportableReport<>();
		Map<Long, List<FuelEffRateStatSection>> sectionMap = new HashMap<>();
		List<GenSection> genSectionList = getSectionListFor(reportGen.getId());
		for(GenSection genSection: genSectionList) {
			List<FuelEffRateStatSection> sectionStatList = new ArrayList<>();
			Long trackerId = genSection.getTrackerId();
			List<FuelStatResult> resultList = getFuelStatisticsResultListByReportIdAndTrackerId(reportGen.getId(), trackerId);
			FuelEffRateStatSection sectionStat = new FuelEffRateStatSection();
			List<FuelStat> statList = resultList.stream().map(statResult-> {
				sectionStat.addFuelUsed(statResult.getFuelUsed());
				sectionStat.addDistanceTravelled(statResult.getDistanceTravelled());
				return new FuelStat(statResult);
			}).collect(Collectors.toList());
			sectionStat.setStatList(statList);
			sectionStatList.add(sectionStat);
			sectionMap.put(trackerId, sectionStatList);
		}

		report.setReportGen(reportGen);
		report.setSectionInfoList(genSectionList);
		report.setSectionDataMap(sectionMap);
		return report;
		
	}
*/	
	
	private ExportableReport<FuelEffRateStatSection> getFuelEffRateExportableReport(ReportGen reportGen) {
		ExportableReport<FuelEffRateStatSection> report = new ExportableReport<>();
		Map<Long, List<FuelEffRateStatSection>> sectionMap = new HashMap<>();
		List<GenSection> genSectionList = getSectionListFor(reportGen.getId());
		for(GenSection genSection: genSectionList) {
			List<FuelEffRateStatSection> sectionStatList = new ArrayList<>();
			Long trackerId = genSection.getTrackerId();
			List<FuelStatResult> resultList = getFuelStatisticsResultListByReportIdAndTrackerId(reportGen.getId(), trackerId);
			FuelEffRateStatSection sectionStat = new FuelEffRateStatSection();
			List<FuelStat> statList = resultList.stream().map(statResult-> {
				sectionStat.addFuelUsed(statResult.getFuelUsed());
				sectionStat.addDistanceTravelled(statResult.getDistanceTravelled());
				return new FuelStat(statResult);
			}).collect(Collectors.toList());
			sectionStat.setStatList(statList);
			sectionStatList.add(sectionStat);
			sectionMap.put(trackerId, sectionStatList);
		}

		report.setReportGen(reportGen);
		report.setSectionInfoList(genSectionList);
		report.setSectionDataMap(sectionMap);
		return report;
	}
	private ExportableReport<FillDrainStatSection> getExportableFillDrainReport(ReportGen reportGen) {
		ExportableReport<FillDrainStatSection> report = new ExportableReport<>();
		List<GenSection> genSectionList = getSectionListFor(reportGen.getId());
		Map<Long, List<FillDrainStatSection>> sectionMap = new HashMap<>();
		
		for(GenSection genSection: genSectionList) {
			List<FillDrainStatSection> sectionStatList = new ArrayList<>();
			Long trackerId = genSection.getTrackerId();
			List<FillDrainStatistics> statList = getFillDrainStatisticsByReportIdAndTrackerId(reportGen.getId(), trackerId);
			
			FillDrainStatSection sectionStat = new FillDrainStatSection();
			sectionStat.setFillingList(statList.stream().filter(stat -> stat.getType().equals(FillDrainStatSection.TYPE_FILLING)).collect(Collectors.toList()));
			sectionStat.setDrainingList(statList.stream().filter(stat -> stat.getType().equals(FillDrainStatSection.TYPE_DRAINING)).collect(Collectors.toList()));
			sectionStat.setStatList(statList);
			sectionStatList.add(sectionStat);
			sectionMap.put(trackerId, sectionStatList);
		}

		report.setReportGen(reportGen);
		report.setSectionInfoList(genSectionList);
		report.setSectionDataMap(sectionMap);
		return report;
	}
	
	
	
}
