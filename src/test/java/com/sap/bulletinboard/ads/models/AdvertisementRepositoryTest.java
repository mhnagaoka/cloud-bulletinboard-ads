package com.sap.bulletinboard.ads.models;

import com.sap.bulletinboard.ads.config.EmbeddedDatabaseConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

import java.sql.Timestamp;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = EmbeddedDatabaseConfig.class)
public class AdvertisementRepositoryTest {

    @Inject
    private AdvertisementRepository repo;

    @Test
    public void shouldSetIdOnFirstSave() {
        Advertisement entity = new Advertisement();
        entity.setTitle("title");
        entity = repo.save(entity);
        assertThat(entity.getId(), is(notNullValue()));
    }

    @Test
    public void shouldSetCreatedTimestampOnFirstSaveOnly() throws InterruptedException{
        Advertisement entity = new Advertisement();
        entity.setTitle("title");

        entity = repo.save(entity);
        Timestamp timestampAfterCreation = entity.getCreatedAt();
        assertThat(timestampAfterCreation, is(notNullValue()));

        entity.setTitle("Updated Title");
        Thread.sleep(5); //Better: mock time!

        entity = repo.save(entity);
        Timestamp timestampAfterUpdate = entity.getCreatedAt();
        assertThat(timestampAfterUpdate, is(timestampAfterCreation));
    }

    @Test
    public void shouldSetUpdatedTimestampOnEveryUpdate() throws InterruptedException{
        Advertisement entity = new Advertisement();
        entity.setTitle("title");
        entity = repo.save(entity);

        entity.setTitle("Updated Title");
        entity = repo.save(entity);
        Timestamp timestampAfterFirstUpdate = entity.getUpdatedAt();
        assertThat(timestampAfterFirstUpdate, is(notNullValue()));

        Thread.sleep(5); //Better: mock time!

        entity.setTitle("Updated Title 2");
        entity = repo.save(entity);
        Timestamp timestampAfterSecondUpdate = entity.getUpdatedAt();
        assertThat(timestampAfterSecondUpdate, is(not(timestampAfterFirstUpdate)));
    }

    @Test(expected = JpaOptimisticLockingFailureException.class)
    public void shouldUseVersionForConflicts() {
        Advertisement entity = new Advertisement();
        entity.setTitle("some title");
        entity = repo.save(entity); // persists entity and sets initial version

        entity.setTitle("entity instance 1");
        Advertisement updatedEntity = repo.save(entity); // returns instance with updated version

        repo.save(entity); // tries to persist entity with outdated version
    }
}
