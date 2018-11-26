package br.edu.ulbra.election.voter.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class VoteClientService {

	private final VoteClient voteClient;

	@Autowired
	public VoteClientService(VoteClient voteClient) {
		this.voteClient = voteClient;
	}

	public Boolean verifyVoter(Long id) {
		return this.voteClient.verificaVoter(id);
	}

	@FeignClient(value = "vote-service", url = "${url.vote-service}")
	private interface VoteClient {

		@GetMapping("/v1/vote/{voterId}")
		Boolean verificaVoter(@PathVariable(name = "voterId") Long voterId);
	}
}
