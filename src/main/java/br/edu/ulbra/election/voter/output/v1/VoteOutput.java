package br.edu.ulbra.election.voter.output.v1;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Election Output Information")
public class VoteOutput {

	@ApiModelProperty(example = "1", notes = "Vote Unique Identifier")
	private Long id;
	@ApiModelProperty(example = "2", notes = "Election Unique Identification")
	private Long electionId;
	@ApiModelProperty(example = "3", notes = "Voter Unique Identifier")
	private Long voterId;
	@ApiModelProperty(example = "4", notes = "Candidate Unique Identifier")
	private Long candidateId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getElectionId() {
		return electionId;
	}

	public void setElectionId(Long electionId) {
		this.electionId = electionId;
	}

	public Long getVoterId() {
		return voterId;
	}

	public void setVoterId(Long voterId) {
		this.voterId = voterId;
	}

	public Long getCandidateId() {
		return candidateId;
	}

	public void setCandidateId(Long candidateId) {
		this.candidateId = candidateId;
	}
}
