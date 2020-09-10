package uk.gov.companieshouse.scanupondemand.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.scanupondemand.orders.api.model.ScanUponDemandItem;
import uk.gov.companieshouse.scanupondemand.orders.api.repository.ScanUponDemandItemRepository;

import java.time.LocalDateTime;

/**
 * Service for the management and storage of scan upon demand items.
 */
@Service
public class ScanUponDemandItemService {

	private final ScanUponDemandItemRepository repository;
	private final IdGeneratorService idGenerator;
	private final EtagGeneratorService etagGenerator;
	private final LinksGeneratorService linksGenerator;

	public ScanUponDemandItemService(final ScanUponDemandItemRepository repository,

			final IdGeneratorService idGenerator, final EtagGeneratorService etagGenerator,
			final LinksGeneratorService linksGenerator) {
		this.repository = repository;
		this.idGenerator = idGenerator;
		this.etagGenerator = etagGenerator;
		this.linksGenerator = linksGenerator;
	}

	/**
	 * Creates the scan upon demand item in the database.
	 *
	 * @param item the item to be created
	 * @return the created item
	 */
	public ScanUponDemandItem createScanUponDemandItem(final ScanUponDemandItem item) {
		item.setId(idGenerator.autoGenerateId());
		setCreationDateTimes(item);
		item.setEtag(etagGenerator.generateEtag());
		item.setLinks(linksGenerator.generateLinks(item.getId()));
		return repository.save(item);
	}

	/**
	 * Sets the created at and updated at date time 'timestamps' to now.
	 *
	 * @param item the item to be 'timestamped'
	 */
	void setCreationDateTimes(final ScanUponDemandItem item) {
		final LocalDateTime now = LocalDateTime.now();
		item.setCreatedAt(now);
		item.setUpdatedAt(now);
	}

}