package domain.bikes;

import domain.DomainEvent;

public class BikeAcceptedEvent extends DomainEvent {

	long bikeId;

	public BikeAcceptedEvent(long bikeId) {
		this.bikeId = bikeId;
	}

	public long getBikeId() {
		return bikeId;
	}
}
