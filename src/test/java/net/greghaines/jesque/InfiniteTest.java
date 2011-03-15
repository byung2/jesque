package net.greghaines.jesque;

import static net.greghaines.jesque.TestUtils.createJedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.greghaines.jesque.worker.Worker;
import net.greghaines.jesque.worker.WorkerImpl;

import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;

public class InfiniteTest
{
	private static final Config config = new ConfigBuilder().withJobPackage("net.greghaines.jesque").build();
	
	@Before
	public void resetRedis()
	throws Exception
	{
		final Jedis jedis = createJedis(config);
		try
		{
			jedis.flushDB();
		}
		finally
		{
			jedis.quit();
		}
	}
	
	@Test
	public void dummy(){}
	
	@SuppressWarnings("unchecked")
//	@Test
	public void dontStopNow()
	throws InterruptedException
	{
		for (int i = 0; i < 5; i++)
		{
			final List<Job> jobs = new ArrayList<Job>(30);
			for (int j = 0; j < 30; j++)
			{
				jobs.add(new Job("TestAction", new Object[]{j, 2.3, true, "test", Arrays.asList("inner", 4.5)}));
			}
			TestUtils.enqueueJobs("foo" + i, jobs, config);
			jobs.clear();
			for (int j = 0; j < 5; j++)
			{
				jobs.add(new Job("FailAction"));
			}
			TestUtils.enqueueJobs("bar", jobs, config);
		}
		final Worker worker = new WorkerImpl(config, Arrays.asList("bar","baz"), Arrays.asList(TestAction.class, FailAction.class));
		final Thread workerThread = new Thread(worker);
		workerThread.start();
		
		TestUtils.enqueueJobs("inf", Arrays.asList(new Job("InfiniteAction")), config);
		final Worker worker2 = new WorkerImpl(config, Arrays.asList("inf"), Arrays.asList(InfiniteAction.class));
		final Thread workerThread2 = new Thread(worker2);
		workerThread2.start();
		
		workerThread.join();
	}
}