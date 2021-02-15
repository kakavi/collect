package org.odk.collect.android.sync;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kakavi on 7/11/2017.
 */
public class KengaJobCreator implements JobCreator {
    @Override
    public Job create(String tag) {
        switch (tag) {
            case PrefillSyncJob.TAG:
                return new PrefillSyncJob();
            default:
                return null;
        }
    }

    public static void scheduleJobs(){
        PrefillSyncJob.scheduleJob();
    }

    public static void cancelAllForTagIfMany(String tag){
        List<JobRequest> allJobsForTag = new ArrayList<>(JobManager.instance().getAllJobRequestsForTag(tag));
        if(allJobsForTag.size()>1){
            JobManager.instance().cancelAllForTag(tag);
        }
    }
}
