package com.amazonaws.ec2;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DryRunResult;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
//import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;

public class ShutdownEC2 {

	final static AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
	public static boolean instanceStatus(String instanceId) {
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
		boolean running = false;
		boolean done = false;

		DescribeInstancesRequest request = new DescribeInstancesRequest();
		while(!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);

			for(Reservation reservation : response.getReservations()) {
				for(Instance instance : reservation.getInstances()) {
					String currInstanceId = instance.getInstanceId();
					if(currInstanceId.equals(instanceId)) {
						String InstanceStatus= instance.getState().getName();
						System.out.println("Current Status : " + InstanceStatus); 
						if(InstanceStatus.equals("running")) {
							running = true;
						}
					}
				}
			}
			request.setNextToken(response.getNextToken());

			if(response.getNextToken() == null) {
				done = true;
			}
		}
		return running;
	}

	public static void stopInstance(String instance_id)
	{
		try {
			if(!instanceStatus(instance_id)) {
				System.out.println("System is not running");
				return;
			}
			System.out.println("System is running"); 
			// Dry run to test shutdown
			DryRunSupportedRequest<StopInstancesRequest> dry_request =
					() -> {
						StopInstancesRequest request = new StopInstancesRequest()
								.withInstanceIds(instance_id);

						return request.getDryRunRequest();
					};

					DryRunResult<StopInstancesRequest> dry_response = ec2.dryRun(dry_request);

					if(!dry_response.isSuccessful()) {
						System.out.printf(
								"Failed dry run to stop instance %s", instance_id);
						throw dry_response.getDryRunResponse();
					}

					// Dry run successful hence shutting down.
					StopInstancesRequest request = new StopInstancesRequest()
							.withInstanceIds(instance_id);

					ec2.stopInstances(request);
				

					System.out.printf("Successfully stop instance %s", instance_id);
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String instance_id= "i-00a28ba72d9efb723";
		stopInstance(instance_id);
		

	}

}
