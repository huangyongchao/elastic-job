/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.scheduler.statistics;

import java.util.Map;
import java.util.Properties;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.plugins.management.ShutdownHookPlugin;
import org.quartz.simpl.SimpleThreadPool;

import com.dangdang.ddframe.job.cloud.scheduler.statistics.job.StatisticJob;
import com.dangdang.ddframe.job.exception.JobStatisticException;

/**
 * 统计作业调度器.
 *
 * @author liguangyun
 */
class StatisticsScheduler {
    
    private final Scheduler scheduler;
    
    /**
     * 构造函数.
     */
    StatisticsScheduler() {
        scheduler = getScheduler();
        try {
            scheduler.start();
        } catch (final SchedulerException ex) {
            throw new JobStatisticException(ex);
        }
    }
    
    private Scheduler getScheduler() {
        StdSchedulerFactory factory = new StdSchedulerFactory();
        try {
            factory.initialize(getQuartzProperties());
            return factory.getScheduler();
        } catch (final SchedulerException ex) {
            throw new JobStatisticException(ex);
        }
    }
    
    private Properties getQuartzProperties() {
        Properties result = new Properties();
        result.put("org.quartz.threadPool.class", SimpleThreadPool.class.getName());
        result.put("org.quartz.threadPool.threadCount", Integer.toString(1));
        result.put("org.quartz.scheduler.instanceName", "ELASTIC_JOB_CLOUD_STATISTICS_SCHEDULER");
        result.put("org.quartz.plugin.shutdownhook.class", ShutdownHookPlugin.class.getName());
        result.put("org.quartz.plugin.shutdownhook.cleanShutdown", Boolean.TRUE.toString());
        return result;
    }
    
    /**
     * 注册统计作业.
     * 
     * @param statisticJob 统计作业
     */
    void register(final StatisticJob statisticJob) {
        try {
            JobDetail jobDetail = statisticJob.buildJobDetail();
            Map<String, Object> dataMap = statisticJob.getDataMap();
            for (String each : dataMap.keySet()) {
                jobDetail.getJobDataMap().put(each, dataMap.get(each));
            }
            scheduler.scheduleJob(jobDetail, statisticJob.buildTrigger());
        } catch (final SchedulerException ex) {
            throw new JobStatisticException(ex);
        }
    }
    
    /**
     * 停止调度.
     */
    void shutdown() {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        } catch (final SchedulerException ex) {
            throw new JobStatisticException(ex);
        }
    }
}
