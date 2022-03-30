function loadData(reportKey) {
  const data = JSON.parse(Android.getData(reportKey));
  const keys = Object.keys(data.nameValuePairs);
  keys.forEach((key) => {
    const element = document.getElementById(key);
    if (element !== null && element !== undefined) {
      document.getElementById(key).innerHTML = data.nameValuePairs[key];
    }
  });
  document.getElementById("report_period").innerHTML = Android.getDataPeriod();
  document.getElementById("reporting_facility").innerHTML =
    Android.getReportingFacility();
}
