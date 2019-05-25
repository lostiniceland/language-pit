package infrastructure.persistence;

import domain.wife.ApprovalStatus;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class JpaWifeApprovalStatusConverter implements AttributeConverter<ApprovalStatus, Character> {

	@SuppressWarnings("Duplicates")
	@Override
	public Character convertToDatabaseColumn(ApprovalStatus attribute) {
		return switch (attribute) {
			case Pending -> 'P';
			case Accepted -> 'A';
			case Rejected -> 'R';
		};
	}

	@Override
	public ApprovalStatus convertToEntityAttribute(Character dbData) {
		return switch (dbData) {
			case 'P' -> ApprovalStatus.Pending;
			case 'A' -> ApprovalStatus.Accepted;
			case 'R' -> ApprovalStatus.Rejected;
			default -> throw new IllegalArgumentException("Unknown " + dbData);
		};
	}
}
