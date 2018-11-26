package br.edu.ulbra.election.voter.service;

import br.edu.ulbra.election.voter.client.VoteClientService;
import br.edu.ulbra.election.voter.exception.GenericOutputException;
import br.edu.ulbra.election.voter.input.v1.VoterInput;
import br.edu.ulbra.election.voter.model.Voter;
import br.edu.ulbra.election.voter.output.v1.GenericOutput;
import br.edu.ulbra.election.voter.output.v1.VoteOutput;
import br.edu.ulbra.election.voter.output.v1.VoterOutput;
import br.edu.ulbra.election.voter.repository.VoterRepository;
import feign.FeignException;

import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class VoterService {

    private final VoterRepository voterRepository;

    private final ModelMapper modelMapper;

    private final PasswordEncoder passwordEncoder;
    
    private final VoteClientService voteClientService;

    private static final String MESSAGE_INVALID_ID = "Invalid id";
    private static final String MESSAGE_VOTER_NOT_FOUND = "Voter not found";

    @Autowired
    public VoterService(VoterRepository voterRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder, VoteClientService voteClientService){
        this.voterRepository = voterRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.voteClientService = voteClientService;
    }

    public List<VoterOutput> getAll(){
        Type voterOutputListType = new TypeToken<List<VoterOutput>>(){}.getType();
        return modelMapper.map(voterRepository.findAll(), voterOutputListType);
    }

    public VoterOutput create(VoterInput voterInput) {
        validateInput(voterInput, false);
        
        Voter voter = modelMapper.map(voterInput, Voter.class);
        
        voter.setPassword(Voter.getHashPassword(voter.getPassword()));

        voter = voterRepository.save(voter);
        return modelMapper.map(voter, VoterOutput.class);
    }

    public VoterOutput getById(Long voterId){
        if (voterId == null){
            throw new GenericOutputException(MESSAGE_INVALID_ID);
        }

        Voter voter = voterRepository.findById(voterId).orElse(null);
        if (voter == null){
            throw new GenericOutputException(MESSAGE_VOTER_NOT_FOUND);
        }

        return modelMapper.map(voter, VoterOutput.class);
    }

    public VoterOutput update(Long voterId, VoterInput voterInput) {
        if (voterId == null){
            throw new GenericOutputException(MESSAGE_INVALID_ID);
        }
        validateInput(voterInput, true);
        
        List<VoterOutput> allVotters = this.getAll();
        
        for(VoterOutput vtOut : allVotters) {        	
        	if(vtOut.getEmail().contentEquals(voterInput.getEmail()) && !(vtOut.getId().equals(voterId))) {
        		throw new GenericOutputException("E-mail inserido já existe, tente novamente inserindo um e-mail diferente");
        	}
        }

        Voter voter = voterRepository.findById(voterId).orElse(null);
        if (voter == null){
            throw new GenericOutputException(MESSAGE_VOTER_NOT_FOUND);
        }

        voter.setEmail(voterInput.getEmail());
        voter.setName(voterInput.getName());
        if (!StringUtils.isBlank(voterInput.getPassword())) {
            voter.setPassword(passwordEncoder.encode(voterInput.getPassword()));
        }
        voter = voterRepository.save(voter);
        return modelMapper.map(voter, VoterOutput.class);
    }

    public GenericOutput delete(Long voterId) {
        if (voterId == null){
            throw new GenericOutputException(MESSAGE_INVALID_ID);
        }

        Voter voter = voterRepository.findById(voterId).orElse(null);
        if (voter == null){
            throw new GenericOutputException(MESSAGE_VOTER_NOT_FOUND);
        }
        
        try {
            List<VoteOutput> votes = voteClientService.getByVoterId(voterId);
            if (votes.size() > 0) {
            	throw new GenericOutputException("This voter already voted!");
            }
        } catch (FeignException e) {
        	if (e.status() == 500) {
                throw new GenericOutputException(MESSAGE_VOTER_NOT_FOUND);
            }
        }
           
        voterRepository.delete(voter);

        return new GenericOutput("Voter deleted");
    }

    private void validateInput(VoterInput voterInput, boolean isUpdate){
    	
        List<VoterOutput> allVotters = this.getAll();
        
        if(!isUpdate) {
            for(VoterOutput vtOut : allVotters) {        	
            	if(vtOut.getEmail().contentEquals(voterInput.getEmail())) {
            		throw new GenericOutputException("E-mail inserido já existe, tente novamente inserindo um e-mail diferente");
            	}
            }	
        }
        
        Integer maxName = voterInput.getName().length();
        Integer nM = voterInput.getName().split(" ").length;
        
        if(maxName < 5) {
        	throw new GenericOutputException("Nome deve ter no mínimo cinco letras.");
        }
        
        if(nM < 2) {
        	throw new GenericOutputException("Nome deve ter no mínimo um sobrenome.");
        }
        
        if (StringUtils.isBlank(voterInput.getEmail())){
            throw new GenericOutputException("E-mail inválido");
        }
        if (StringUtils.isBlank(voterInput.getName())){
            throw new GenericOutputException("Nome Inválido");
        }
        if (!StringUtils.isBlank(voterInput.getPassword())){
            if (!voterInput.getPassword().equals(voterInput.getPasswordConfirm())){
                throw new GenericOutputException("Senhas não coincidem");
            }
        } else {
            if (!isUpdate) {
                throw new GenericOutputException("Senhas não podem estar vazias");
            }
        }
    }

}
