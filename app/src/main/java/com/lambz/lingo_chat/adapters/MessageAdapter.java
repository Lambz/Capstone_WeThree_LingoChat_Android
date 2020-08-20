package com.lambz.lingo_chat.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lambz.lingo_chat.R;
import com.lambz.lingo_chat.Utils;
import com.lambz.lingo_chat.models.Message;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>
{
    private final String messageSenderID;
    private List<Message> mMessageList;
    private Context mContext;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private Translate mTranslate;

    public MessageAdapter(List<Message> messageList, Context context, Translate translate)
    {
        this.mMessageList = messageList;
        this.mContext = context;
        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        this.mTranslate = translate;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.custom_message_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        Message message = mMessageList.get(position);
        if (message.getFrom().equals(messageSenderID))
        {
            holder.senderLayout.setVisibility(View.VISIBLE);
            holder.receiverLayout.setVisibility(View.GONE);
            if (message.getType().equals("text"))
            {
                holder.senderImageView.setVisibility(View.GONE);
                holder.senderTextView.setVisibility(View.VISIBLE);
                holder.senderTextView.setText(message.getText());
                new Thread(() ->
                {
                    Translation translation = mTranslate.translate(message.getText(), Translate.TranslateOption.targetLanguage(Utils.getLanguageCode()));
                    new Handler(Looper.getMainLooper()).post(() -> holder.senderTextView.setText(translation.getTranslatedText()));
                }).start();
            } else if(message.getType().equals("image"))
            {
                holder.senderImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getLink()).error(R.drawable.image_error).into(holder.senderImageView);
                holder.senderTextView.setVisibility(View.GONE);
            }
            else
            {
                holder.senderImageView.setVisibility(View.VISIBLE);
                holder.senderTextView.setVisibility(View.GONE);
            }
        } else
        {
            holder.receiverLayout.setVisibility(View.VISIBLE);
            holder.senderLayout.setVisibility(View.GONE);
            if (message.getType().equals("text"))
            {
                holder.receiverImageView.setVisibility(View.GONE);
                holder.receiverTextView.setVisibility(View.VISIBLE);
                holder.receiverTextView.setText(message.getText());
                new Thread(() ->
                {
                    Translation translation = mTranslate.translate(message.getText(), Translate.TranslateOption.targetLanguage(Utils.getLanguageCode()));
                    new Handler(Looper.getMainLooper()).post(() -> holder.receiverTextView.setText(translation.getTranslatedText()));
                }).start();
            } else if(message.getType().equals("image"))
            {
                holder.receiverImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getLink()).error(R.drawable.file).into(holder.receiverImageView);
                holder.receiverTextView.setVisibility(View.GONE);
            } else
            {
                holder.receiverImageView.setVisibility(View.VISIBLE);
                holder.receiverTextView.setVisibility(View.GONE);
            }
        }
        if(position == 0)
        {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.layout.getLayoutParams();
            params.setMargins(0,getPx(55),0,0);
            holder.layout.setLayoutParams(params);
        }

        if(message.getFrom().equals(messageSenderID))
        {
            holder.itemView.setOnClickListener(view ->
            {
                if(message.getType().equals("pdf") || message.getType().equals("docx"))
                {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete for me",
                                    "Delete and for all",
                                    "Download and View this document",
                                    "Cancel"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setItems(options, (dialogInterface, i) ->
                    {
                        if(i==0)
                        {

                        }
                        else if(i==1)
                        {

                        }
                        else if(i==2)
                        {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getLink()));
                            mContext.startActivity(intent);
                        }
                    });
                    builder.show();
                }
                else if(message.getType().equals("text"))
                {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete for me",
                                    "Delete and for all",
                                    "Cancel"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setItems(options, (dialogInterface, i) ->
                    {
                        if(i==0)
                        {

                        }
                        else if(i==1)
                        {

                        }
                    });
                    builder.show();
                }else if(message.getType().equals("image"))
                {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete for me",
                                    "Delete and for all",
                                    "View this Image",
                                    "Cancel"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setItems(options, (dialogInterface, i) ->
                    {
                        if(i==0)
                        {

                        }
                        else if(i==1)
                        {

                        }
                        else if(i==2)
                        {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getLink()));
                            mContext.startActivity(intent);
                        }
                    });
                    builder.show();
                }
            });
        }
        else
        {
            holder.itemView.setOnClickListener(view ->
            {
                if(message.getType().equals("pdf") || message.getType().equals("docx"))
                {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete for me",
                                    "Download and View this document",
                                    "Cancel"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setItems(options, (dialogInterface, i) ->
                    {
                        if(i==0)
                        {

                        }
                        else if(i==1)
                        {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getLink()));
                            mContext.startActivity(intent);
                        }
                    });
                    builder.show();
                }
                else if(message.getType().equals("text"))
                {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete for me",
                                    "Cancel"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setItems(options, (dialogInterface, i) ->
                    {
                        if(i==0)
                        {

                        }
                    });
                    builder.show();
                }else if(message.getType().equals("image"))
                {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete for me",
                                    "View this Image",
                                    "Cancel"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setItems(options, (dialogInterface, i) ->
                    {
                        if(i==0)
                        {

                        }
                        else if(i==1)
                        {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getLink()));
                            mContext.startActivity(intent);
                        }
                    });
                    builder.show();
                }
            });
        }
    }

    @Override
    public int getItemCount()
    {
        return mMessageList.size();
    }

    public void setMessageList(List<Message> messageList)
    {
        mMessageList = messageList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private ConstraintLayout layout;
        private LinearLayout senderLayout, receiverLayout;
        private ImageView senderImageView, receiverImageView;
        private TextView senderTextView, receiverTextView;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            senderLayout = itemView.findViewById(R.id.sender_layout);
            receiverLayout = itemView.findViewById(R.id.receiver_layout);
            senderImageView = itemView.findViewById(R.id.sender_imageview);
            receiverImageView = itemView.findViewById(R.id.receiver_imageview);
            senderTextView = itemView.findViewById(R.id.sender_message_textview);
            receiverTextView = itemView.findViewById(R.id.receiver_message_textview);
            layout = itemView.findViewById(R.id.layout);
        }
    }

    private int getPx(int dp)
    {
        Resources r = mContext.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
        return px;
    }

    private void deleteSendMessages(final Message message, final ViewHolder holder, final int position)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(message.getFrom()).child(message.getTo()).child(message.getId()).removeValue().addOnCompleteListener(task ->
        {
            if(task.isSuccessful())
            {
                Toast.makeText(mContext, "Deleted Successfully.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(mContext, "Error Occured while deleting", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteReceiveMessages(final Message message, final ViewHolder holder, final int position)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(message.getTo()).child(message.getFrom()).child(message.getId()).removeValue().addOnCompleteListener(task ->
        {
            if(task.isSuccessful())
            {
                Toast.makeText(mContext, "Deleted Successfully.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(mContext, "Error Occured while deleting", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteMessageForEveryone(final Message message, final ViewHolder holder, final int position)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(message.getTo()).child(message.getFrom()).child(message.getId()).removeValue().addOnCompleteListener(task ->
        {
            if(task.isSuccessful())
            {
                rootRef.child("Messages").child(message.getFrom()).child(message.getTo()).child(message.getId()).removeValue().addOnCompleteListener(task1 ->
                {
                    if(task1.isSuccessful())
                    {
                        Toast.makeText(mContext, "Deleted Successfully.", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(mContext, "Error Occured while deleting", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else
            {
                Toast.makeText(mContext, "Error Occured while deleting", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
