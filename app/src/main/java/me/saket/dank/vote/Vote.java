package me.saket.dank.vote;

import com.google.auto.value.AutoValue;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import io.reactivex.Completable;
import me.saket.dank.data.DankRedditClient;
import me.saket.dank.data.VotableContributionFullNameWrapper;
import me.saket.dank.walkthrough.SyntheticData;
import timber.log.Timber;

public interface Vote {

  PublicContribution contributionToVote();

  VoteDirection direction();

  Completable saveAndSend(VotingManager votingManager);

  static Vote create(PublicContribution contributionToVote, VoteDirection direction) {
    String submissionId;

    if (contributionToVote instanceof Comment) {
      submissionId = ((Comment) contributionToVote).getSubmissionId();

    } else if (contributionToVote instanceof Submission) {
      submissionId = contributionToVote.getId();

    } else {
      throw new AssertionError("Unknown contribution to vote for");
    }

    if (submissionId.equalsIgnoreCase(SyntheticData.SUBMISSION_ID_FOR_GESTURE_WALKTHROUGH)) {
      return new AutoValue_Vote_NoOpVote(contributionToVote, direction);
    }
    return new AutoValue_Vote_RealVote(contributionToVote, direction);
  }

  Completable sendToRemote(DankRedditClient dankRedditClient);

  @AutoValue
  abstract class RealVote implements Vote {

    @Override
    public Completable saveAndSend(VotingManager votingManager) {
      return votingManager.voteWithAutoRetry(this);
    }

    @Override
    public Completable sendToRemote(DankRedditClient dankRedditClient) {
      VotableContributionFullNameWrapper votableThing = VotableContributionFullNameWrapper.createFrom(contributionToVote());
      return dankRedditClient.withAuth(Completable.fromAction(() -> dankRedditClient.userAccountManager().vote(votableThing, direction())));
    }
  }

  @AutoValue
  abstract class NoOpVote implements Vote {

    @Override
    public Completable saveAndSend(VotingManager votingManager) {
      return votingManager.voteWithAutoRetry(this);
    }

    @Override
    public Completable sendToRemote(DankRedditClient dankRedditClient) {
      Timber.i("Ignoring voting in synthetic-submission-for-gesture-walkthrough");
      return Completable.complete();
    }
  }
}