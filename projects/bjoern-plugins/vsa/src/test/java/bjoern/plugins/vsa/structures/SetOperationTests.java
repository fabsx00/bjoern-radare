package bjoern.plugins.vsa.structures;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SetOperationTests
{
	@Test
	public void testUnionNonOverlappingIntervals()
	{
		StridedInterval a = StridedInterval.getStridedInterval(16, 48, 1024, DataWidth.R64);
		StridedInterval b = StridedInterval.getStridedInterval(8, 4, 28, DataWidth.R64);

		StridedInterval expected = StridedInterval.getStridedInterval(4, 4, 1024, DataWidth.R64);

		assertEquals(expected, a.union(b));
	}

	@Test
	public void testUnionPartlyOverlappingIntervals()
	{
		StridedInterval a = StridedInterval.getStridedInterval(16, 48, 1024, DataWidth.R64);
		StridedInterval b = StridedInterval.getStridedInterval(8, 0, 256, DataWidth.R64);

		StridedInterval expected = StridedInterval.getStridedInterval(8, 0, 1024, DataWidth.R64);

		assertEquals(expected, a.union(b));
	}

	@Test
	public void testUnionFullyContainedInterval()
	{
		StridedInterval a = StridedInterval.getStridedInterval(16, 48, 1024, DataWidth.R64);
		StridedInterval b = StridedInterval.getStridedInterval(8, 98, 258, DataWidth.R64);

		StridedInterval expected = StridedInterval.getStridedInterval(2, 48, 1024, DataWidth.R64);

		assertEquals(expected, a.union(b));
	}

	@Test
	public void testUnionCommutativeProperty()
	{
		StridedInterval a = StridedInterval.getStridedInterval(16, 48, 1024, DataWidth.R64);
		StridedInterval b = StridedInterval.getStridedInterval(8, 98, 258, DataWidth.R64);

		assertEquals(a.union(b), b.union(a));
	}

	@Test
	public void testIntersect()
	{
		StridedInterval a = StridedInterval.getStridedInterval(3, 1, 4, DataWidth.R4);
		StridedInterval b = StridedInterval.getStridedInterval(2, 2, 6, DataWidth.R4);

		StridedInterval expected = StridedInterval.getSingletonSet(4, DataWidth.R4);
		assertEquals(expected, a.intersect(b));
	}

	@Test
	public void testIntersectCommutativeProperty()
	{
		StridedInterval a = StridedInterval.getStridedInterval(3, 1, 4, DataWidth.R4);
		StridedInterval b = StridedInterval.getStridedInterval(2, 2, 6, DataWidth.R4);

		assertEquals(b.intersect(a), a.intersect(b));
	}

}
