package br.edu.ulbra.election.voter.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import br.edu.ulbra.election.voter.output.v1.VoteOutput;


@Service
public class VoteClientService {
	private final VoteClient voteClient;

    @Autowired
    public VoteClientService(VoteClient voteClient) {
        this.voteClient = voteClient;
    }

    public List<VoteOutput> getByVoterId(Long voterId) {
        return this.voteClient.getByVoterId(voterId);
    }

    @FeignClient(value="vote-service", url="http://localhost:8081")
    private interface VoteClient {

    	@GetMapping("/v1/vote/voter/{voterId}")
    	List<VoteOutput> getByVoterId(@PathVariable(name = "voterId") Long voterId);
    }
}