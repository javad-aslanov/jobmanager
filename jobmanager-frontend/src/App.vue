<template>
  <div class="job-manager-container">
    <h1 class="title">Job Manager</h1>
    <button class="fetch-button" @click="getAllJobs">Get All Jobs</button>
    <ul class="job-list">
      <li v-for="job in jobs" :key="job.jobid" class="job-item" @click="selectJob(job)">
        <span class="job-name">{{ job.jobname }}</span>
        <span class="job-status">{{ jobStatusToString(job.jobstatus) }}</span>
      </li>
    </ul>

    <div v-if="selectedJob" class="job-details-modal">
      <div class="job-details-content">
        <h2>Job Details</h2>
        <p><strong>Job ID:</strong> {{ selectedJob.jobid }}</p>
        <p><strong>Job Name:</strong> {{ selectedJob.jobname }}</p>
        <p><strong>Status:</strong> {{ jobStatusToString(selectedJob.jobstatus) }}</p>
        <p><strong>Creation Date:</strong> {{ selectedJob.creationdate }}</p>
        <button @click="closeJobDetails" class="close-button">Close</button>
      </div>
    </div>
  </div>
</template>

<script>
import { JobServiceClient } from './generated/job_grpc_web_pb.js';
import { Empty } from './generated/job_pb.js';

const JobStatus = {
  0: 'SUBMITTED',
  1: 'RUNNING',
  2: 'COMPLETED',
};

export default {
  data() {
    return {
      jobs: [],         // List of jobs fetched from the gRPC server
      client: null,     // gRPC client instance
      selectedJob: null // Currently selected job for displaying details
    };
  },
  created() {
    this.client = new JobServiceClient('http://localhost:8085');
  },
  methods: {
    getAllJobs() {
      const request = new Empty();
      this.client.listAllJobs(request, {}, (err, response) => {
        if (err) {
          console.error('Error fetching jobs:', err);
        } else {
          const jobsList = response.getJobsList().map((job) => job.toObject());
          this.jobs = jobsList;
        }
      });
    },
    jobStatusToString(status) {
      return JobStatus[status] || 'UNKNOWN';
    },
    selectJob(job) {
      this.selectedJob = job;
    },
    closeJobDetails() {
      this.selectedJob = null;
    }
  }
};
</script>

<style>
.job-manager-container {
  max-width: 600px;
  margin: 50px auto;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.title {
  font-size: 2rem;
  color: #333;
  text-align: center;
  margin-bottom: 20px;
}

.fetch-button {
  display: block;
  width: 100%;
  padding: 10px 0;
  font-size: 1rem;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.fetch-button:hover {
  background-color: #0056b3;
}

.job-list {
  list-style-type: none;
  padding: 0;
  margin: 20px 0 0;
}

.job-item {
  display: flex;
  justify-content: space-between;
  padding: 10px;
  margin: 10px 0;
  background-color: #fff;
  border-radius: 5px;
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.job-item:hover {
  background-color: #f0f0f0;
}

.job-name {
  font-size: 1.1rem;
  color: #333;
}

.job-status {
  font-size: 1rem;
  color: #555;
  font-style: italic;
}

.job-details-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
}

.job-details-content {
  background-color: #fff;
  padding: 20px;
  border-radius: 10px;
  max-width: 400px;
  width: 100%;
}

.close-button {
  background-color: #ff5555;
  color: white;
  border: none;
  padding: 10px;
  cursor: pointer;
  margin-top: 20px;
  width: 100%;
  border-radius: 5px;
}

.close-button:hover {
  background-color: #cc4444;
}
</style>
