package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.CompanyProfileDto;
import br.com.everton.backendextrato.model.CompanyProfile;
import br.com.everton.backendextrato.repository.CompanyProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CompanyProfileService {

    private final CompanyProfileRepository repository;

    public CompanyProfileService(CompanyProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<CompanyProfileDto> getByUserEmail(String userEmail) {
        return repository.findByUserEmailIgnoreCase(userEmail).map(this::toDto);
    }

    @Transactional
    public CompanyProfileDto save(String userEmail, CompanyProfileDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Dados da empresa são obrigatórios.");
        }

        CompanyProfile profile = repository.findByUserEmailIgnoreCase(userEmail).orElseGet(CompanyProfile::new);
        profile.setUserEmail(userEmail);
        profile.setLegalName(trim(request.legalName()));
        profile.setTradeName(trim(request.tradeName()));
        profile.setCnpj(trim(request.cnpj()));
        profile.setTaxRegime(trim(request.taxRegime()));
        profile.setActivity(trim(request.activity()));
        profile.setStateRegistration(trim(request.stateRegistration()));
        profile.setMunicipalRegistration(trim(request.municipalRegistration()));
        profile.setOpeningDate(trim(request.openingDate()));
        profile.setBusinessPhone(trim(request.businessPhone()));
        profile.setBusinessEmail(trim(request.businessEmail()));
        profile.setWebsite(trim(request.website()));
        profile.setContactName(trim(request.contactName()));
        profile.setZipCode(trim(request.zipCode()));
        profile.setStreet(trim(request.street()));
        profile.setNumber(trim(request.number()));
        profile.setComplement(trim(request.complement()));
        profile.setDistrict(trim(request.district()));
        profile.setCity(trim(request.city()));
        profile.setState(trim(request.state()));
        profile.setInvoiceEmail(trim(request.invoiceEmail()));
        profile.setPixKey(trim(request.pixKey()));
        profile.setMainBank(trim(request.mainBank()));
        profile.setBillingNotes(trim(request.billingNotes()));
        profile.setAccountantName(trim(request.accountantName()));
        profile.setAccountantEmail(trim(request.accountantEmail()));
        profile.setAccountantPhone(trim(request.accountantPhone()));
        profile.setPayrollNotes(trim(request.payrollNotes()));
        profile.setOperationNotes(trim(request.operationNotes()));
        return toDto(repository.save(profile));
    }

    private CompanyProfileDto toDto(CompanyProfile profile) {
        return new CompanyProfileDto(
                profile.getLegalName(),
                profile.getTradeName(),
                profile.getCnpj(),
                profile.getTaxRegime(),
                profile.getActivity(),
                profile.getStateRegistration(),
                profile.getMunicipalRegistration(),
                profile.getOpeningDate(),
                profile.getBusinessPhone(),
                profile.getBusinessEmail(),
                profile.getWebsite(),
                profile.getContactName(),
                profile.getZipCode(),
                profile.getStreet(),
                profile.getNumber(),
                profile.getComplement(),
                profile.getDistrict(),
                profile.getCity(),
                profile.getState(),
                profile.getInvoiceEmail(),
                profile.getPixKey(),
                profile.getMainBank(),
                profile.getBillingNotes(),
                profile.getAccountantName(),
                profile.getAccountantEmail(),
                profile.getAccountantPhone(),
                profile.getPayrollNotes(),
                profile.getOperationNotes()
        );
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
