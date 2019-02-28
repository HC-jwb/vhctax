<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
  	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
  	<link rel="stylesheet" type="text/css" href="/css/jquery-ui.min.css">
	<link rel="stylesheet" type="text/css" href="/css/modules.min.css">
	<link rel="stylesheet" type="text/css" href="/css/scrolltabs.css">
	<link rel="stylesheet" type="text/css" href="/css/report.css">
	<title>연료 소모량 리포트</title>
	<style>
	.ui.fixed.menu {
	padding: 0.25em 0;
	margin: 0;
  	border: 1px solid #ddd;
  	box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.1);
}
	</style>
</head>
<body>
<div class="ui fixed main secondary small blue inverted menu" id="topMenu">
	<div class="ui container">
		<%-- <div class="logo-item"><div class="logo-container"><img class="ui image" src="/images/logo.png"></div></div> --%>
		<a class="fuel report active enabled item"><i class="bar chart outline icon"></i> 연비 분석</a>
		<a class="filling report item"><i class="chart line icon"></i> 주유 분석</a>
		<a class="tracking enabled item"><i class="road icon"></i> 운행관제</a>
		<div class="right menu">
			<a class="ui logout item" href="/logout" title="로그아웃"><i class="sign-out icon"></i></a>
		</div>
	</div>
</div>

<div class="main-container">
	<div class="left-column">
		<div class="ui raised segment">
			<div class="ui blue top attached label"><i class="line bar chart icon"></i>연비분석 보고서</div>	
				<div class="ui relaxed divided tiny selection list" id="genReportList"></div>
		</div>
	</div>
	<div class="right-column">
		<div class="ui small accordion" id="reportGenAccordion">
			<div class="title"><i class="dropdown icon"></i><div class="ui teal tag label">연비 보고서 생성하기</div></div>
			<div class="content">
				<div class="ui fluid small form" id="reportGenFrm">
					<div class="fields">
						<div class="seven wide required field">
							<label>보고서 제목</label>
							<div class="ui small input">
								<input type="text" placeholder="보고서 제목 및 설명" name="description">
							</div>
						</div>
					</div>
					<div class="fields">
						<div class="ui three wide required field">
							<label>그룹선택</label>
							<div class="ui fluid search selection dropdown" id="trackerGroupListDropdown">
								<input type="hidden" name="trackerGroup">
								<div class="default text">그룹 선택</div>
								<div class="ui menu">
									<div class="item" data-value="0">메인그룹</div>
								</div>
							</div>
						</div>
						<div class="ui four wide required field">
							<label>차량선택</label>
							<div class="ui fluid search selection dropdown" id="trackerListDropdown">
								<i class="dropdown icon"></i>
								<input type="hidden" name="trackerList">
								<div class="default text">차량 선택</div>
								<div class="ui menu">
								</div>
							</div>
						</div>
						<div class="ui three wide required field">
							<label>시작일</label>
							<div class="ui small left icon input">
								<input type="text" placeholder="시작일" id="fromDate" name="fromDate"><i class="calendar alternate outline icon"></i>
							</div>
						</div>
						<div class="ui three wide required field">
							<label>종료일</label>	
							<div class="ui small icon input">
								<input type="text" placeholder="종료일" id="toDate" name="toDate"><i class="calendar alternate outline icon"></i>
							</div>
						</div>
					</div>
					<div class="fields">
						<div class="ui thirteen wide field">
							<div style="text-align: right;">
								<div class="ui small close compact button">닫기</div>
								<div class="ui small blue compact generate button">보고서 생성</div>
							</div>
						</div>
					</div>
					<div class="fields"><div class="thirteen wide error field"></div></div>
				</div><!--  end of form-->
			</div><!-- end of content -->
		</div><!-- end of accordion -->
		<div class="ui divider" style="margin-top: 0; margin-bottom: 0.5em;"></div>
		<div id="scrolltabsContainer" style="display:none;">
			<div style="width: 100%; text-align: right;"><div class="ui compact right labeled icon small teal button excel-download">보고서 다운로드<i class="file excel outline icon"></i></div></div>
			<ul id="scrollTabs" class="scroll_tabs_theme_dark"></ul>
			<div id="statTableContainer" style="display:none; height:100%;">
				<div class="ui loader"></div>
				<table class="ui celled striped compact small table" style="margin-top: 0;">
					<thead>
					<tr>
					<th class="center aligned">일자</th>
					<th class="center aligned">연료소비량(L)</th>
					<th class="center aligned">운행거리(KM)</th>
					<th class="center aligned">연비(KM/L)</th>
					</tr>
					</thead>
					<tbody></tbody>
				</table>
			</div>
		</div>
	</div><!--  end of right column -->
</div><!-- end of main -->
<script src="/js/jquery-3.1.1.min.js"></script>
<script src="/js/jquery-ui.min.js"></script>
<script src="/js/modules.min.js"></script>
<script src="/js/jquery.scrolltabs.js"></script>
<script src="/js/jquery.mousewheel.js"></script>
<script src="/js/report-api.js"></script>
<script src="/js/report.js"></script>
</body>
</html>