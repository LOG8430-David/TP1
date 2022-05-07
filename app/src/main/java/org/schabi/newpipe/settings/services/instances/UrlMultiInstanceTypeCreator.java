package org.schabi.newpipe.settings.services.instances;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import org.schabi.newpipe.R;
import org.schabi.newpipe.databinding.DialogAddInstanceBinding;
import org.schabi.newpipe.extractor.instance.Instance;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UrlMultiInstanceTypeCreator<I extends Instance>
        extends AbstractInstanceTypeCreator<I> {

    protected final Function<String, I> createNewInstanceFromUrl;
    /**
     * The (official) list of available instances.
     */
    @StringRes
    protected final Integer instanceListUrl;

    /**
     * List of {@link StringRes} representing the requirements for an instance (e.g. a working API).
     */
    protected final List<Integer> instanceRequirements;

    /**
     * <code>true</code> if the service is free and open source software.
     */
    protected final boolean isFoss;

    public UrlMultiInstanceTypeCreator(
            final String instanceServiceName,
            final int icon,
            final Class<I> createdClass,
            final Function<String, I> createNewInstanceFromUrl,
            @StringRes final Integer instanceListUrl,
            final List<Integer> instanceRequirements,
            final boolean isFoss
    ) {
        super(instanceServiceName, icon, createdClass);
        this.createNewInstanceFromUrl = createNewInstanceFromUrl;
        this.instanceListUrl = instanceListUrl;
        this.instanceRequirements = instanceRequirements;
        this.isFoss = isFoss;
    }

    @Override
    public void createNewInstance(
            @NonNull final Context context,
            @NonNull final List<? extends Instance> existingInstances,
            @NonNull final Consumer<I> onInstanceCreated
    ) {
        showAddInstanceUrlDialog(context, url ->
                defaultValidateAndCleanUrl(url, context, existingInstances, createdClass())
                        .map(createNewInstanceFromUrl)
                        .ifPresent(onInstanceCreated));
    }

    @SuppressLint("SetTextI18n")
        // When using non concated text for
        // instanceRequirements, we would have to duplicate code/translations
    void showAddInstanceUrlDialog(
            final Context c,
            final Consumer<String> onUrlCreated
    ) {
        final DialogAddInstanceBinding dialogBinding
                = DialogAddInstanceBinding.inflate(LayoutInflater.from(c));
        if (instanceListUrl != -1) {
            dialogBinding.instanceHelp.setVisibility(View.VISIBLE);
            dialogBinding.instanceHelp.setText(
                    c.getString(
                            R.string.publicly_available_instances_help,
                            c.getString(instanceListUrl)));
        }
        if (!instanceRequirements.isEmpty()) {
            dialogBinding.instanceRequirements.setVisibility(View.VISIBLE);
            dialogBinding.instanceRequirements.setText(
                    c.getString(R.string.note_that_an_instance_requires_the_following)
                            + instanceRequirements.stream()
                            .map(c::getString)
                            .map(s -> "\n • " + s)
                            .collect(Collectors.joining())
            );
        }
        dialogBinding.fossNoticeContainer.setVisibility(isFoss ? View.VISIBLE : View.GONE);

        new AlertDialog.Builder(c)
                .setTitle(c.getString(R.string.add_instance, instanceServiceName()))
                .setIcon(icon())
                .setView(dialogBinding.getRoot())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog1, which) ->
                        onUrlCreated.accept(dialogBinding.dialogEditText.getText().toString())
                )
                .show();
    }

    /**
     * Validates and cleans an url.
     * <br/>
     * Does the following:
     * <ul>
     *     <li>Adds "https://" if the url is not starting with "http"</li>
     *     <li>Removes trailing slashes</li>
     *     <li>Notifies the user if no "https://" url is used</li>
     *     <li>
     *         Checks if the url is already used by an instance of the same type (targetedClass)
     *     </li>
     * </ul>
     *
     *
     * @param url              The inputted url
     * @param context          Context
     * @param currentInstances Currently active instances
     * @param targetedClass    The targeted/created class
     * @return The validated and cleaned url
     */
    Optional<String> defaultValidateAndCleanUrl(
            final String url,
            final Context context,
            final List<? extends Instance> currentInstances,
            final Class<? extends Instance> targetedClass
    ) {
        String cleanUrl = url.trim();
        // if protocol not present, add https
        if (!cleanUrl.startsWith("http")) {
            cleanUrl = "https://" + cleanUrl;
        }
        // remove trailing slash
        cleanUrl = cleanUrl.replaceAll("/$", "");
        // consider using HTTPS
        if (!cleanUrl.startsWith("https://")) {
            Toast.makeText(context,
                    R.string.http_is_insecure_consider_using_https,
                    Toast.LENGTH_LONG).show();
        }
        // only allow if not already exists (and it's the same instance)
        for (final Instance instance : currentInstances
                .stream()
                .filter(targetedClass::isInstance)
                .collect(Collectors.toList())
        ) {
            if (instance.getUrl().equals(cleanUrl)) {
                Toast.makeText(context,
                        R.string.instance_already_exists,
                        Toast.LENGTH_LONG).show();
                return Optional.empty();
            }
        }
        return Optional.of(cleanUrl);
    }
}
