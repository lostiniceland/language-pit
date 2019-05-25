package infrastructure.persistence;

import domain.bikes.ApprovalStatus;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class JpaBikeApprovalStatusConverter implements AttributeConverter<ApprovalStatus, String> {

	@SuppressWarnings("Duplicates")
	@Override
	public String convertToDatabaseColumn(ApprovalStatus attribute) {
		return switch (attribute) {
			case Pending -> "P";
			case Accepted -> "A";
			case Rejected -> "R";
		};
	}

	@Override
	public ApprovalStatus convertToEntityAttribute(String dbData) {
		return switch (dbData) {
			case "P" -> ApprovalStatus.Pending;
			case "A" -> ApprovalStatus.Accepted;
			case "R" -> ApprovalStatus.Rejected;
			default -> throw new IllegalArgumentException("Unknown " + dbData);
		};
	}
}
