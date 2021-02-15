package org.odk.collect.android.sync;

import androidx.annotation.NonNull;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import org.odk.collect.android.application.Collect;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by kakavi on 7/11/2017.
 */
public class PrefillSyncJob extends Job {

    public static final String TAG = "PrefillSyncJob_tag";
    public static long intervalMs = TimeUnit.HOURS.toMillis(24);
    public static long flexMs = TimeUnit.MINUTES.toMillis(5);//wait before run

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
//        run your Job here
        boolean isBackgroundJob = this.getParams().isPeriodic()?true:false;
        new KengaSyncroniser(Collect.getInstance()).syncPrefillData(isBackgroundJob);
        return Result.SUCCESS;
    }

    public static void scheduleJob() {
        KengaJobCreator.cancelAllForTagIfMany(TAG);
        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putString("jobId", "prefill_sync_job");
        new JobRequest.Builder(PrefillSyncJob.TAG)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setPeriodic(intervalMs, flexMs)
                .setRequirementsEnforced(true)
                .setExtras(extras)
                .build()
                .schedule();
        runJobImmediately();
    }

    public static String getLastSync() {
        List<JobRequest> allJobsForTag = new ArrayList<>(JobManager.instance().getAllJobRequestsForTag(TAG));

        if (!allJobsForTag.isEmpty()) {
            long lastRun = allJobsForTag.get(0).getLastRun();
            if (lastRun > 0) {
                return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(lastRun));
            }else{
                return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(allJobsForTag.get(0).getScheduledAt()));
            }
        }
        return "NA";
    }

    public static String getNextSync(){
        List<JobRequest> allJobsForTag = new ArrayList<>(JobManager.instance().getAllJobRequestsForTag(TAG));
        if (!allJobsForTag.isEmpty()) {
            JobRequest jobRequest = allJobsForTag.get(0);
            long nextRun = jobRequest.getLastRun()+intervalMs;
            if (jobRequest.getLastRun() > 0){
                return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(nextRun));
            }else{
                return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(jobRequest.getScheduledAt()+intervalMs));
            }
        }
        return "NA";
    }

    public static void runJobImmediately() {
        int jobId = new JobRequest.Builder(PrefillSyncJob.TAG)
                .startNow()
                .build()
                .schedule();
    }

}
