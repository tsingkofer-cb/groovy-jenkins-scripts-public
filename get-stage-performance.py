# Collect performance metrics (min/max/avg/median duration) for a specific stage of a pipeline job.
# This script relies on the Pipeline Stage View Rest API, which will by default return data from a maximum of the 10 most recent builds
# This can be overridden with a system property (com.cloudbees.workflow.rest.external.JobExt.maxRunsPerJob) but consider that this can affect UI performance as Stage View will then load more data for all jobs

# Usage: python get-stage-performance.py --user $JENKINS_USER_ID --password $JENKINS_API_TOKEN --jobUrl $JOB_URL --stageName $STAGE_NAME

import json, requests, sys, getpass
from requests.auth import HTTPBasicAuth
from statistics import median
import argparse

class StagePerformance:
    def __init__(self, id, duration):
        self.id = id #Build Number
        self.duration = duration #Stage duration in ms

parser = argparse.ArgumentParser(description="Collect stage performance data.")
parser.add_argument('--user', help='CI username', required=True)
parser.add_argument('--password', help='CI API token, will prompt if not provided')
parser.add_argument('--jobUrl', help='Job URL to retrieve stage data from.', required=True)
parser.add_argument('--stageName', help='Name of stage to retrieve.', required=True)
parser.add_argument('--debug', help='Print stage durations from all builds.', action='store_true')
args = parser.parse_args()

jobUrl = args.jobUrl
stageName = args.stageName

if not args.password:
    p = getpass.getpass()
else:
    p = args.password
auth = HTTPBasicAuth(args.user, p) 

stagePerformanceData = []
jobResponse = requests.get(jobUrl + '/wfapi/runs', auth=auth).json()
for build in jobResponse:
    stages = build["stages"]
    for stage in stages:
        if stage["name"] == stageName:
            stagePerformanceData.append(StagePerformance(build["id"],stage["durationMillis"]))

if args.debug:
    for data in stagePerformanceData:
        print("Build Number: " + str(data.id) +", Duration: " + str(data.duration))

durationList = [i.duration for i in stagePerformanceData]
buildCount = len(stagePerformanceData)
duration_avg = sum(durationList) / buildCount
duration_med = median(durationList)
duration_min = min(durationList)
duration_max = max(durationList)


print("Here are the statistics for the last " + str(buildCount) + " builds of the '" + stageName + "' stage in job " + jobUrl)
print("Minimum duration: " + str(duration_min/1000) + " seconds")
print("Maximum duration: " + str(duration_max/1000) + " seconds")
print("Average duration: " + str(duration_avg/1000) + " seconds")
print("Median duration: " + str(duration_med/1000) + " seconds")
