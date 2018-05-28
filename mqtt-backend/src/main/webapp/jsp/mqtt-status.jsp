<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>

	<head>
		<link rel="stylesheet" href="../css/bootstrap.min.css">
		<script type="text/javascript" src="../js/jquery1.11.0.min.js"></script>
		<script src="../js/bootstrap.min.js"></script>
	</head>

	<body>
		<div class="container">
			<h2>RDB MQTT INNER</h2>
			<c:if test="${not empty message}">
				<div class="alert alert-success">${message}</div>
			</c:if>
			<c:set var="poolStateMap" value="${mqtt.poolQueue.poolStates }" />
			<div class="basic-info">
				<a class="btn btn-primary" role="button" data-toggle="collapse" href="#collapseExample" aria-expanded="false" aria-controls="collapseExample">
					Basic Info
				</a>
				<div class="collapse" id="collapseExample">
					<div class="well">
						<form class="form-horizontal">
							<div class="form-group">
								<label class="col-sm-2 control-label">Client Id</label>
								<div class="col-sm-10">
									<p class="form-control-static">${mqtt.clientId}</p>
								</div>
							</div>
							<div class="form-group">
								<label class="col-sm-2 control-label">Broker</label>
								<div class="col-sm-10">
									<p class="form-control-static">${mqtt.broker}</p>
								</div>
							</div>
							<div class="form-group">
								<label class="col-sm-2 control-label">Topic</label>
								<div class="col-sm-10">
									<p class="form-control-static">${mqtt.topic}</p>
								</div>
							</div>
							<div class="form-group">
								<label class="col-sm-2 control-label">Client Connection Timeout(second)</label>
								<div class="col-sm-10">
									<p class="form-control-static">${mqtt.connectionTimeout}</p>
								</div>
							</div>
							<div class="form-group">
								<label class="col-sm-2 control-label">Client Keep Alive Interval(second)</label>
								<div class="col-sm-10">
									<p class="form-control-static">${mqtt.keepAliveInterval}</p>
								</div>
							</div>
							<div class="form-group">
								<label class="col-sm-2 control-label">Batch Insert Count</label>
								<div class="col-sm-10">
									<p class="form-control-static">${mqtt.appConfig.insertBatchCount}</p>
								</div>
							</div>
							<div class="form-group">
								<label class="col-sm-2 control-label">Empty Queue Alive Time(second)</label>
								<div class="col-sm-10">
									<p class="form-control-static">${mqtt.appConfig.maxProcessInactive}</p>
								</div>
							</div>
						</form>
					</div>
				</div>
			</div>
			<hr />
			<div>
				<lable>Cached INSERT Data:${mqtt.processCacher.size()}</lable>
			</div>
			<c:choose>
				<c:when test="${not empty poolStateMap}">
					<table class="table table-striped">
						<thead>
							<tr>
								<td>#</td>
								<td>Queue Name</td>
								<td>Queue Size</td>
								<td>Queue Last Timestamp</td>
								<td>Refresh Timestamp</td>
							</tr>
						</thead>
						<c:forEach var="queueState" items="${poolStateMap}" varStatus="mapStatu">
							<tr>
								<td>${mapStatu.index+1 }</td>
								<td>${queueState.key }</td>
								<td>${queueState.value.refQueueSize }</td>
								<td>${queueState.value.lastedProduced }</td>
								<td>${refreshTime }</td>
							</tr>
						</c:forEach>
					</table>
				</c:when>
				<c:otherwise>
					<br>
					<div class="alert alert-info">No queue found in pool</div>
				</c:otherwise>
			</c:choose>
			<!--<button type="submit" class="btn btn-primary  btn-md">New employee</button>-->
		</div>
	</body>

</html>