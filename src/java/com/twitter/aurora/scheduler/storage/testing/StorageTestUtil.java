package com.twitter.aurora.scheduler.storage.testing;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;

import org.easymock.Capture;
import org.easymock.IAnswer;
import org.easymock.IExpectationSetters;

import com.twitter.aurora.gen.ScheduledTask;
import com.twitter.aurora.gen.TaskQuery;
import com.twitter.aurora.scheduler.storage.AttributeStore;
import com.twitter.aurora.scheduler.storage.JobStore;
import com.twitter.aurora.scheduler.storage.QuotaStore;
import com.twitter.aurora.scheduler.storage.SchedulerStore;
import com.twitter.aurora.scheduler.storage.Storage;
import com.twitter.aurora.scheduler.storage.Storage.MutableStoreProvider;
import com.twitter.aurora.scheduler.storage.Storage.MutateWork;
import com.twitter.aurora.scheduler.storage.Storage.StoreProvider;
import com.twitter.aurora.scheduler.storage.Storage.Work;
import com.twitter.aurora.scheduler.storage.TaskStore;
import com.twitter.aurora.scheduler.storage.UpdateStore;
import com.twitter.common.testing.EasyMockTest;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;

/**
 * Auxiliary class to simplify testing against a mocked storage.  This allows callers to directly
 * set up call expectations on individual stores rather than writing plumbing code to deal with
 * operations and {@link StoreProvider}.
 */
public class StorageTestUtil {

  public final StoreProvider storeProvider;
  public final MutableStoreProvider mutableStoreProvider;
  public final TaskStore.Mutable taskStore;
  public final QuotaStore.Mutable quotaStore;
  public final AttributeStore.Mutable attributeStore;
  public final JobStore.Mutable jobStore;
  public final UpdateStore.Mutable updateStore;
  public final SchedulerStore.Mutable schedulerStore;
  public final Storage storage;

  /**
   * Creates a new storage test utility.
   *
   * @param easyMock Mocking framework to use for setting up mocks and expectations.
   */
  public StorageTestUtil(EasyMockTest easyMock) {
    this.storeProvider = easyMock.createMock(StoreProvider.class);
    this.mutableStoreProvider = easyMock.createMock(MutableStoreProvider.class);
    this.taskStore = easyMock.createMock(TaskStore.Mutable.class);
    this.quotaStore = easyMock.createMock(QuotaStore.Mutable.class);
    this.attributeStore = easyMock.createMock(AttributeStore.Mutable.class);
    this.jobStore = easyMock.createMock(JobStore.Mutable.class);
    this.updateStore = easyMock.createMock(UpdateStore.Mutable.class);
    this.schedulerStore = easyMock.createMock(SchedulerStore.Mutable.class);
    this.storage = easyMock.createMock(Storage.class);
  }

  private <T> IExpectationSetters<T> expectConsistentRead() {
    final Capture<Work<T, RuntimeException>> work = EasyMockTest.createCapture();
    return expect(storage.consistentRead(capture(work))).andAnswer(new IAnswer<T>() {
      @Override public T answer() {
        return work.getValue().apply(storeProvider);
      }
    });
  }

  private <T> IExpectationSetters<T> expectWeaklyConsistentRead() {
    final Capture<Work<T, RuntimeException>> work = EasyMockTest.createCapture();
    return expect(storage.weaklyConsistentRead(capture(work))).andAnswer(new IAnswer<T>() {
      @Override public T answer() {
        return work.getValue().apply(storeProvider);
      }
    });
  }

  private <T> IExpectationSetters<T> expectWriteOperation() {
    final Capture<MutateWork<T, RuntimeException>> work = EasyMockTest.createCapture();
    return expect(storage.write(capture(work))).andAnswer(new IAnswer<T>() {
      @Override public T answer() {
        return work.getValue().apply(mutableStoreProvider);
      }
    });
  }

  /**
   * Expects any number of read or write operations.
   */
  public void expectOperations() {
    expect(storeProvider.getTaskStore()).andReturn(taskStore).anyTimes();
    expect(storeProvider.getQuotaStore()).andReturn(quotaStore).anyTimes();
    expect(storeProvider.getAttributeStore()).andReturn(attributeStore).anyTimes();
    expect(storeProvider.getJobStore()).andReturn(jobStore).anyTimes();
    expect(storeProvider.getUpdateStore()).andReturn(updateStore).anyTimes();
    expect(storeProvider.getSchedulerStore()).andReturn(schedulerStore).anyTimes();
    expect(mutableStoreProvider.getTaskStore()).andReturn(taskStore).anyTimes();
    expect(mutableStoreProvider.getUnsafeTaskStore()).andReturn(taskStore).anyTimes();
    expect(mutableStoreProvider.getQuotaStore()).andReturn(quotaStore).anyTimes();
    expect(mutableStoreProvider.getAttributeStore()).andReturn(attributeStore).anyTimes();
    expect(mutableStoreProvider.getJobStore()).andReturn(jobStore).anyTimes();
    expect(mutableStoreProvider.getUpdateStore()).andReturn(updateStore).anyTimes();
    expect(mutableStoreProvider.getSchedulerStore()).andReturn(schedulerStore).anyTimes();
    expectConsistentRead().anyTimes();
    expectWeaklyConsistentRead().anyTimes();
    expectWriteOperation().anyTimes();
  }

  public IExpectationSetters<?> expectTaskFetch(TaskQuery query, ScheduledTask... result) {
    return expect(taskStore.fetchTasks(query))
        .andReturn(ImmutableSet.<ScheduledTask>builder().add(result).build());
  }

  public IExpectationSetters<?> expectTaskFetch(
      Supplier<TaskQuery> query, ScheduledTask... result) {

    return expect(taskStore.fetchTasks(query))
        .andReturn(ImmutableSet.<ScheduledTask>builder().add(result).build());
  }
}